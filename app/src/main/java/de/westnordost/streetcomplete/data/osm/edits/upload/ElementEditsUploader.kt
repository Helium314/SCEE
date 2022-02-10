package de.westnordost.streetcomplete.data.osm.edits.upload

import android.content.Context
import android.util.Log
import de.westnordost.streetcomplete.data.download.DownloadController
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.IsRevertAction
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapData
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApi
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataUpdates
import de.westnordost.streetcomplete.data.osm.mapdata.MutableMapData
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.data.upload.UploadService
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class ElementEditsUploader(
    private val elementEditsController: ElementEditsController,
    private val mapDataController: MapDataController,
    private val singleUploader: ElementEditUploader,
    private val mapDataApi: MapDataApi,
    private val downloadController: DownloadController,
    private val context: Context
) {
    var uploadedChangeListener: OnUploadedChangeListener? = null

    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + CoroutineName("ElementEditsUploader"))

    suspend fun upload() = mutex.withLock { withContext(Dispatchers.IO) {
        while (true) {
            val edit = elementEditsController.getOldestUnsynced() ?: break
            if (downloadController.isPriorityDownloadInProgress) {
                // background: delay by 1s, then start upload
                scope.async {
                    delay(1000)
                    context.startService(UploadService.createIntent(context))
                }
                break
            }
            val idProvider = elementEditsController.getIdProvider(edit.id)
            /* the sync of local change -> API and its response should not be cancellable because
             * otherwise an inconsistency in the data would occur. E.g. no "star" for an uploaded
             * change, a change could be uploaded twice etc */
            withContext(scope.coroutineContext) { uploadEdit(edit, idProvider) }
        }
    } }

    private suspend fun uploadEdit(edit: ElementEdit, idProvider: ElementIdProvider) {
        val questTypeName = edit.questType::class.simpleName!!
        val editActionClassName = edit.action::class.simpleName!!

        try {
            val updates = singleUploader.upload(edit, idProvider)

            Log.d(TAG, "Uploaded a $editActionClassName")
            uploadedChangeListener?.onUploaded(questTypeName, edit.position)

            elementEditsController.markSynced(edit, updates)
            mapDataController.updateAll(updates)

        } catch (e: ConflictException) {
            Log.d(TAG, "Dropped a $editActionClassName: ${e.message}")
            uploadedChangeListener?.onDiscarded(questTypeName, edit.position)

            elementEditsController.markSyncFailed(edit)

            val mapData = fetchElementComplete(edit.elementType, edit.elementId)
            if (mapData != null) {
                mapDataController.updateAll(MapDataUpdates(updated = mapData.toList()))
            } else {
                val elementKey = ElementKey(edit.elementType, edit.elementId)
                mapDataController.updateAll(MapDataUpdates(deleted = listOf(elementKey)))
            }
        }
    }

    private suspend fun fetchElementComplete(elementType: ElementType, elementId: Long): MapData? =
        withContext(Dispatchers.IO) {
            when (elementType) {
                ElementType.NODE -> mapDataApi.getNode(elementId)?.let { MutableMapData(listOf(it)) }
                ElementType.WAY -> mapDataApi.getWayComplete(elementId)
                ElementType.RELATION -> mapDataApi.getRelationComplete(elementId)
            }
        }

    companion object {
        private const val TAG = "ElementEditsUploader"
    }
}
