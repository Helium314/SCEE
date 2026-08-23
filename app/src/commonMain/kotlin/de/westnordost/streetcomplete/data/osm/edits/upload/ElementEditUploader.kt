package de.westnordost.streetcomplete.data.osm.edits.upload

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.ApplicationConstants.EDIT_ACTIONS_NOT_ALLOWED_TO_USE_LOCAL_CHANGES
import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsManager
import de.westnordost.streetcomplete.data.osm.mapdata.ElementIdUpdate
import de.westnordost.streetcomplete.data.osm.mapdata.ChangesetTooLargeException
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApiClient
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataSource
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataUpdates
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.RemoteMapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.user.UserLoginController
import de.westnordost.streetcomplete.util.ktx.copy
import de.westnordost.streetcomplete.util.Mockable

@Mockable
class ElementEditUploader(
    private val changesetManager: OpenChangesetsManager,
    private val mapDataApi: MapDataApiClient,
    private val mapDataSource: MapDataSource
) {

    /** Apply the given change to the given element and upload it
     *
     *  @throws ConflictException if element has been changed server-side in an incompatible way
     *  @throws IllegalArgumentException if element edit produces invalid data (e.g. if
     *          key or value of any OSM tag is too long)
     */
    suspend fun upload(edit: ElementEdit, getIdProvider: () -> ElementIdProvider): MapDataUpdates {
        // certain edit types don't allow building changes on top of cached map data
        val mustUseRemoteData = edit.action::class in EDIT_ACTIONS_NOT_ALLOWED_TO_USE_LOCAL_CHANGES

        if (ApplicationConstants.DEBUG && !UserLoginController.loggedIn)
            return fakeUpload(edit, getIdProvider)

        return if (mustUseRemoteData) {
            uploadUsingRemoteRepo(edit, getIdProvider)
        } else {
            // we first try to apply the changes onto the element cached locally, then upload...
            try {
                val localChanges = edit.action.createUpdates(mapDataSource, getIdProvider())
                try {
                    uploadChanges(edit, localChanges, false)
                }
                // changeset already too large -> try again with new changeset
                catch (e: ChangesetTooLargeException) {
                    uploadChanges(edit, localChanges, true)
                }
            }
            // ...but this can fail for various reasons:
            // - the changeset is already closed on remote
            // - the element was modified on remote in the meantime
            // - there's a conflict when applying the change to the locally cached element
            // - the element does not exist in the local database (cache was deleted)
            //
            // In any case -> try again with remote data
            catch (e: ConflictException) {
                uploadUsingRemoteRepo(edit, getIdProvider)
            }
        }
    }

    /**
     *  Apply the given edit to data downloaded ad-hoc from remote, then upload it.
     *
     *  @throws ConflictException if element has been changed on remote in an incompatible way
     * */
    private suspend fun uploadUsingRemoteRepo(edit: ElementEdit, getIdProvider: () -> ElementIdProvider): MapDataUpdates {
        // If a conflict is thrown here, it definitely means that the element has been changed on
        // remote in an incompatible way. So, we don't catch the exception but exit
        val remoteChanges = edit.action.createUpdates(RemoteMapDataRepository(mapDataApi), getIdProvider())

        val updates = try {
            uploadChanges(edit, remoteChanges, false)
        }
        // probably changeset was closed -> try again once with new changeset
        catch (e: ConflictException) {
            uploadChanges(edit, remoteChanges, true)
        }
        // changeset too large -> also try again once with new changeset
        catch (e: ChangesetTooLargeException) {
            uploadChanges(edit, remoteChanges, true)
        }

        // We need to backfill any updates of ways which include nodes that we don't have in the
        // local database already with these nodes from remote. Otherwise, when new nodes have been
        // added to the updated way, we'd end up with incomplete ways.
        // We only need to do that in this method because when just uploading local changes without
        // conflict, there can't be any new elements referenced by the updated way except those we
        // added ourselves and thus already know.
        return backfillMapDataUpdates(updates)
    }

    private suspend fun uploadChanges(
        edit: ElementEdit,
        changes: MapDataChanges,
        newChangeset: Boolean
    ): MapDataUpdates {
        val changesetId = if (newChangeset) {
            changesetManager.createChangeset(edit.type, edit.source, edit.position)
        } else {
            changesetManager.getOrCreateChangeset(edit.type, edit.source, edit.position, edit.isNearUserLocation)
        }
        return mapDataApi.uploadChanges(changesetId, changes, ApplicationConstants::ignoreRelation)
    }

    /** Ensures that all nodes of all updated ways in [updates] are either already present in the
     *  local database or otherwise then also included in the result. */
    private suspend fun backfillMapDataUpdates(updates: MapDataUpdates): MapDataUpdates {
        val nodeIdsOfUpdatedWays = updates.updated
            .filterIsInstance<Way>()
            .flatMapTo(HashSet()) { it.nodeIds }

        val idsOfUpdatedNodes =
            updates.updated.filterIsInstance<Node>().mapTo(HashSet()) { it.id } +
            updates.idUpdates.map { it.newElementId }

        val nodeIdsThatMustBePresentInLocalData = nodeIdsOfUpdatedWays - idsOfUpdatedNodes

        val presentNodeIds = mapDataSource
            .getNodes(nodeIdsThatMustBePresentInLocalData)
            .mapTo(HashSet()) { it.id }

        val nodesThatMustBeFetchedFromRemote = nodeIdsThatMustBePresentInLocalData - presentNodeIds

        return if (nodesThatMustBeFetchedFromRemote.isNotEmpty()) {
            val nodes = nodesThatMustBeFetchedFromRemote.mapNotNull { mapDataApi.getNode(it) }
            updates.copy(updated = updates.updated + nodes)
        } else {
            updates
        }
    }

    // fake upload in debug mode: create pseudo-random new (positive!) ids that are unlikely to clash with real ids
    // useful for testing upload
    private fun fakeUpload(edit: ElementEdit, getIdProvider: () -> ElementIdProvider): MapDataUpdates {
        val localChanges = edit.action.createUpdates(mapDataSource, getIdProvider())
        val creationsByNewId = localChanges.creations.associateBy { Long.MAX_VALUE - Int.MAX_VALUE + it.hashCode() }
        val updates = MapDataUpdates(
            updated = (localChanges.modifications + creationsByNewId.map { it.value.copy(id = it.key) })
                .map { element ->
                    // need to update node ids of ways, don't care about relations
                    if (element is Way && element.nodeIds.any { it < 0 }) {
                        val newNodeIds = element.nodeIds.map { id ->
                            if (id > 0) id
                            else
                                creationsByNewId.entries.first { it.value.id == id }.key
                        }
                        element.copy(nodeIds = newNodeIds)
                    } else element
                },
            deleted = localChanges.deletions.map { it.key },
            idUpdates = creationsByNewId.map { ElementIdUpdate(it.value.type, it.value.id, it.key) }
        )
        return updates
    }
}
