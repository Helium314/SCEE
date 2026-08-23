package de.westnordost.streetcomplete.screens.main

import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestController
import de.westnordost.streetcomplete.data.location.SurveyChecker
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestSource
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestSource
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.ExternalSourceQuestKey
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenController
import de.westnordost.streetcomplete.util.ktx.launch
import de.westnordost.streetcomplete.util.ktx.truncateTo6Decimals
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.enlargedBy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
abstract class MainBottomSheetViewModel : ViewModel() {
    abstract val shownBottomSheet: StateFlow<ShownBottomSheet?>

    abstract val geometryOffsetInWindow: MutableStateFlow<Offset?>

    abstract fun showCreateElementInOverlay(overlay: Overlay)

    abstract fun showElementInOverlay(overlay: Overlay, elementKey: ElementKey)

    abstract fun showQuest(questKey: QuestKey)

    abstract fun showCreateNote(trackpoints: List<Trackpoint>?)

    abstract fun closeBottomSheet()

    abstract fun hideQuest(questKey: QuestKey, tempHide: Boolean)

    abstract fun isSurvey(geometry: ElementGeometry): Boolean

    abstract fun submitEdit(
        elementEditType: ElementEditType,
        geometry: ElementGeometry,
        elementEditAction: ElementEditAction,
    )
    abstract fun commentNote(
        note: Note,
        text: String?,
        imagePaths: List<String> = emptyList(),
        close: Boolean
    )
    abstract fun createNote(
        position: LatLon,
        text: String,
        imagePaths: List<String> = emptyList(),
        trackpoints: List<Trackpoint>? = null,
        isGpx: Boolean = false
    )
}

@Stable
class MainBottomSheetViewModelImpl(
    private val mapDataSource: MapDataWithEditsSource,
    private val notesSource: NotesWithEditsSource,
    private val osmQuestSource: OsmQuestSource,
    private val osmNoteQuestSource: OsmNoteQuestSource,
    private val elementEditsController: ElementEditsController,
    private val noteEditsController: NoteEditsController,
    private val hiddenQuestsController: QuestsHiddenController,
    private val surveyChecker: SurveyChecker,
    private val externalSource: ExternalSourceQuestController,
    private val visibleQuestsSource: VisibleQuestsSource,
    private val prefs: Preferences,
) : MainBottomSheetViewModel() {
    override val shownBottomSheet = MutableStateFlow<ShownBottomSheet?>(null)

    override val geometryOffsetInWindow = MutableStateFlow<Offset?>(null)

    override fun closeBottomSheet() {
        shownBottomSheet.value = null
    }

    override fun showCreateElementInOverlay(overlay: Overlay) {
        shownBottomSheet.value = ShownBottomSheet.Overlay(overlay, null, null)
    }

    override fun showElementInOverlay(overlay: Overlay, elementKey: ElementKey) {
        launch(Dispatchers.IO) {
            showElementInOverlayOrNote(overlay, elementKey)
        }
    }

    private suspend fun showElementInOverlayOrNote(overlay: Overlay, elementKey: ElementKey) {
        val geometry = mapDataSource.getGeometry(elementKey.type, elementKey.id) ?: return

        // a note at the position of the element blocks editing of that element
        val note = getNoteForElementAt(geometry.center)
        if (note != null) {
            val quest = osmNoteQuestSource.get(note.id) ?: return
            shownBottomSheet.value = ShownBottomSheet.OsmNoteQuest(quest, note)
        } else {
            val element = mapDataSource.get(elementKey.type, elementKey.id) ?: return
            shownBottomSheet.value = ShownBottomSheet.Overlay(overlay, element, geometry)
        }
    }

    override fun showQuest(questKey: QuestKey) {
        launch(Dispatchers.IO) {
            when (questKey) {
                is OsmNoteQuestKey -> showOsmNoteQuest(questKey)
                is OsmQuestKey -> showOsmQuest(questKey)
                is ExternalSourceQuestKey -> showExternalSourceQuest(questKey)
            }
        }
    }

    override fun showCreateNote(trackpoints: List<Trackpoint>?) {
        shownBottomSheet.value = ShownBottomSheet.CreateOsmNote(trackpoints)
    }

    override fun hideQuest(questKey: QuestKey, tempHide: Boolean) {
        launch(Dispatchers.IO) {
            if (tempHide) hiddenQuestsController.tempHide(questKey)
            else hiddenQuestsController.hide(questKey)
        }
    }

    override fun isSurvey(geometry: ElementGeometry): Boolean =
        surveyChecker.checkIsSurvey(geometry)

    override fun submitEdit(
        elementEditType: ElementEditType,
        geometry: ElementGeometry,
        elementEditAction: ElementEditAction,
    ) {
        launch(Dispatchers.IO) {
            val isNearUserLocation = surveyChecker.checkIsSurvey(geometry)
            elementEditsController.add(elementEditType, geometry, "survey", elementEditAction, isNearUserLocation, (shownBottomSheet.value as? ShownBottomSheet.ExternalSourceQuest)?.quest?.key)
            if (elementEditType !is OsmElementQuestType<*> || !prefs.getBoolean(Prefs.SHOW_NEXT_QUEST_IMMEDIATELY, false))
                return@launch
            val quest = visibleQuestsSource.getAll(geometry.center.enclosingBoundingBox(0.5))
                .filterIsInstance<OsmQuest>()
                .firstOrNull { it.geometry == geometry && it.type.dotColor == null }
            if (quest == null) return@launch
            shownBottomSheet.value = ShownBottomSheet.OsmQuest(quest, mapDataSource.get(quest.elementType, quest.elementId)!!)
        }
    }

    override fun commentNote(
        note: Note,
        text: String?,
        imagePaths: List<String>,
        close: Boolean
    ) {
        launch(Dispatchers.IO) {
            val action = if (close) NoteEditAction.CLOSE else NoteEditAction.COMMENT
            noteEditsController.add(note.id, action, note.position, text, imagePaths)
        }
    }

    override fun createNote(
        position: LatLon,
        text: String,
        imagePaths: List<String>,
        trackpoints: List<Trackpoint>?,
        isGpx: Boolean
    ) {
        launch(Dispatchers.IO) {
            noteEditsController.add(0, NoteEditAction.CREATE, position, text, imagePaths, trackpoints, isGpx)
        }
    }

    private fun showOsmQuest(questKey: OsmQuestKey) {
        val element = mapDataSource.get(questKey.elementType, questKey.elementId) ?: return
        val quest = osmQuestSource.get(questKey) ?: return
        shownBottomSheet.value = ShownBottomSheet.OsmQuest(quest, element)
    }

    private fun showExternalSourceQuest(questKey: ExternalSourceQuestKey) {
        val quest = externalSource.get(questKey) ?: return
        shownBottomSheet.value = ShownBottomSheet.ExternalSourceQuest(quest)
    }

    private fun showOsmNoteQuest(questKey: OsmNoteQuestKey) {
        val note = notesSource.get(questKey.noteId) ?: return
        val quest = osmNoteQuestSource.get(questKey.noteId) ?: return
        shownBottomSheet.value = ShownBottomSheet.OsmNoteQuest(quest, note)
    }

    private fun getNoteForElementAt(position: LatLon): Note? {
        return notesSource
            .getAll(BoundingBox(position, position).enlargedBy(0.2))
            .filter { note ->
                note.position.truncateTo6Decimals() == position.truncateTo6Decimals() &&
                hiddenQuestsController.get(OsmNoteQuestKey(note.id)) == null
            }.firstOrNull()
    }
}

/** The data necessary to show an element from the map clicked on in the bottom sheet */
sealed interface ShownBottomSheet {
    data class OsmQuest(
        val quest: de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest,
        val element: Element,
    ) : ShownBottomSheet {
        override val position get() = quest.position
    }

    data class ExternalSourceQuest(
        val quest: de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuest,
    ) : ShownBottomSheet {
        override val position get() = quest.position
    }

    data class OsmNoteQuest(
        val quest: de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuest,
        val note: Note
    ) : ShownBottomSheet {
        override val position get() = quest.position
    }

    data class Overlay(
        val overlay: de.westnordost.streetcomplete.data.overlays.Overlay,
        val element: Element?,
        val geometry: ElementGeometry?,
    ) : ShownBottomSheet {
        override val position get() = geometry?.center
    }

    data class CreateOsmNote(
        val trackpoints: List<Trackpoint>?
    ) : ShownBottomSheet {
        override val position get() = null
    }

    data class AddPoi(override val position: LatLon, val feature: Feature) : ShownBottomSheet

    data class InsertNode(override val position: LatLon) : ShownBottomSheet {
        var highlightGeometries: ((Collection<ElementGeometry>) -> Unit)? = null
    }

    val position: LatLon?
}
