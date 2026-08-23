package de.westnordost.streetcomplete.screens.main.bottom_sheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuest
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.meta.get
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.move.MoveNodeAction
import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitWayAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.osmquests.Action
import de.westnordost.streetcomplete.osm.AccessManagerDialog
import de.westnordost.streetcomplete.osm.ConstructionDialog
import de.westnordost.streetcomplete.osm.places.applyReplacePlaceTo
import de.westnordost.streetcomplete.osm.places.getPlaceAsDisused
import de.westnordost.streetcomplete.quests.custom.CustomQuestList
import de.westnordost.streetcomplete.quests.shop_type.ShopGoneDialog
import de.westnordost.streetcomplete.quests.shop_type.ShopType
import de.westnordost.streetcomplete.quests.shop_type.ShopTypeAnswer
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.main.bottom_sheet.move_node.MoveNodeForm
import de.westnordost.streetcomplete.screens.main.bottom_sheet.note.LeaveNoteInsteadForm
import de.westnordost.streetcomplete.screens.main.bottom_sheet.split_way.SplitWayForm
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.CantSayDialog
import de.westnordost.streetcomplete.ui.common.quest.ConfirmDeleteDialog
import de.westnordost.streetcomplete.ui.common.quest.LocalElement
import de.westnordost.streetcomplete.ui.common.quest.LocalMapMarkersCallback
import de.westnordost.streetcomplete.ui.common.quest.LocalMapMetersPerDp
import de.westnordost.streetcomplete.ui.common.quest.LocalMapRotation
import de.westnordost.streetcomplete.ui.common.quest.LocalMapTilt
import de.westnordost.streetcomplete.ui.common.quest.LocalQuestType
import de.westnordost.streetcomplete.ui.common.quest.Marker
import de.westnordost.streetcomplete.ui.util.ReplaceBottomSheetTransitionSpec
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.util.ktx.geometryType
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Container in which all quest forms are housed.
 *
 *  Takes care of showing the forms for the "other answers" (leave note, split way, move node)
 *  and associated confirmation dialogs and animates between the overlay form and those.
 *
 *  @param onSetMapMarkers is called when the form shown wishes to show markers on the map. E.g. the
 *         split way form and level form shows markers
 */
@Composable
fun ExternalSourceQuestFormContainer(
    onDismiss: () -> Unit,
    onEdit: (action: ElementEditAction) -> Unit,
    onLeaveNote: (noteText: String, noteImagePaths: List<String>, isGpx: Boolean) -> Unit,
    onHideQuest: (tempHide: Boolean) -> Unit,
    quest: ExternalSourceQuest,
    geometryOffsetInWindow: Offset?,
    mapPosition: LatLon?,
    mapRotation: Float,
    mapTilt: Float,
    mapMetersPerDp: Double,
    onSetMapMarkers: (Iterable<Marker>) -> Unit,
    modifier: Modifier = Modifier,
    countryBoundaries: CountryBoundaries = koinInject(),
    featureDictionary: FeatureDictionary = koinInject(),
    countryInfos: CountryInfos = koinInject(),
) {
    val center = quest.geometry.center
    val countryInfo = remember(center) { countryInfos.get(countryBoundaries, center) }

    var confirmSplitWay by remember { mutableStateOf(false) }
    var confirmMoveNode by remember { mutableStateOf(false) }
    var confirmDeletePoi by remember { mutableStateOf(false) }
    var confirmReplacePlace by remember { mutableStateOf(false) }
    var confirmCantSay by remember { mutableStateOf(false) }
    var showAccessManager by remember { mutableStateOf(false) }
    var showConstructionDialog by remember { mutableStateOf(false) }

    var state by rememberSerializable { mutableStateOf<QuestFormState2>(QuestFormState2.Quest) }

    // markers shown are per-form
    LaunchedEffect(state) { onSetMapMarkers(emptyList()) }

    fun onAction(action: Action) {
        when (action) {
            Action.Dismiss -> onDismiss()
            Action.LeaveNote -> state = QuestFormState2.LeaveNote
            Action.HideQuest -> onHideQuest(false)
            Action.TempHideQuest -> onHideQuest(true)
            Action.CantSay -> confirmCantSay = true
            Action.SplitWay -> confirmSplitWay = true
            Action.MoveNode -> confirmMoveNode = true
            Action.DeletePoi -> confirmDeletePoi = true
            Action.ReplacePoi -> confirmReplacePlace = true
            Action.EditTags -> state = QuestFormState2.EditTags
            Action.ManageAccess -> showAccessManager = true
            Action.UnderConstruction -> showConstructionDialog = true
        }
    }

    val mapDataSource: MapDataWithEditsSource = koinInject()
    val element = remember { quest.elementKey?.let { mapDataSource.get(it.type, it.id) } }

    CompositionLocalProvider(
        LocalQuestType provides quest.type,
        LocalElement provides element,
        LocalMapRotation provides mapRotation,
        LocalMapTilt provides mapTilt,
        LocalMapMetersPerDp provides mapMetersPerDp,
        LocalMapMarkersCallback provides onSetMapMarkers
    ) {
        AnimatedContent(
            targetState = state,
            transitionSpec = ReplaceBottomSheetTransitionSpec,
            modifier = modifier,
        ) { currentState ->
            when (currentState) {
                QuestFormState2.Quest -> {
                    quest.type.Form(
                        on = ::onAction,
                        quest = quest,
                        countryInfo = countryInfo
                    )
                }
                QuestFormState2.LeaveNote -> {

                    LeaveNoteInsteadForm(
                        onLeaveNote = { text, noteImagePaths, isGpx ->
                            onLeaveNote(text, noteImagePaths, isGpx)
                        },
                        onDismiss = onDismiss,
                        editType = quest.type,
                        element = element,
                    )
                }
                QuestFormState2.SplitWay -> {
                    SplitWayForm(
                        onConfirmed = { onEdit(SplitWayAction(element, it)) },
                        onDismiss = onDismiss,
                        mapPosition = mapPosition,
                        way = element as Way,
                        wayGeometry = quest.geometry as ElementPolylinesGeometry,
                    )
                }
                QuestFormState2.MoveNode -> {
                    MoveNodeForm(
                        onConfirmed = { onEdit(MoveNodeAction(element, it)) },
                        onDismiss = onDismiss,
                        mapPosition = mapPosition,
                        nodeOffsetInWindow = geometryOffsetInWindow,
                        node = element as Node,
                        elementEditType = quest.type,
                    )
                }
                QuestFormState2.EditTags -> {
                    EditTagsForm(
                        onConfirmed = { onEdit(UpdateElementTagsAction(element!!, it)) },
                        onDismiss = onDismiss,
//                        editType = quest.type,
                    )
                }
            }
        }
    }

    if (confirmSplitWay) {
        ConfirmationDialog(
            onDismissRequest = { confirmSplitWay = false },
            onConfirmed = { state = QuestFormState2.SplitWay },
            text = { Text(stringResource(Res.string.quest_split_way_description)) }
        )
    }
    if (confirmMoveNode) {
        ConfirmationDialog(
            onDismissRequest = { confirmMoveNode = false },
            onConfirmed = { state = QuestFormState2.MoveNode },
            text = { Text(stringResource(Res.string.quest_move_node_message)) }
        )
    }
    if (confirmReplacePlace) {
        ShopGoneDialog(
            onDismissRequest = { confirmReplacePlace = false },
            onSelectAnswer = { answer ->
                when (answer) {
                    is ShopType -> {
                        val builder = StringMapChangesBuilder(element.tags)
                        answer.feature.applyReplacePlaceTo(builder)
                        onEdit(UpdateElementTagsAction(element, builder.create()))
                    }
                    ShopTypeAnswer.IsShopVacant -> {
                        val vacantShop = featureDictionary.getPlaceAsDisused(element, country = countryInfo.countryOrSubdivisionCode)
                        val builder = StringMapChangesBuilder(element.tags)
                        vacantShop.applyReplacePlaceTo(builder)
                        onEdit(UpdateElementTagsAction(element, builder.create()))
                    }
                    ShopTypeAnswer.LeaveNote -> {
                        state = QuestFormState2.LeaveNote
                    }
                }
            },
            featureDictionary = featureDictionary,
            geometryType = element!!.geometryType,
            countryCode = countryInfo.countryOrSubdivisionCode,
        )
    }
    if (confirmDeletePoi) {
        ConfirmDeleteDialog(
            onDismissRequest = { confirmDeletePoi = false },
            onConfirmDelete = {
                onEdit(DeletePoiNodeAction(element as Node))
            },
            onLeaveNote = {
                state = QuestFormState2.LeaveNote
            }
        )
    }
    if (confirmCantSay) {
        val customQuestList: CustomQuestList = koinInject()
        CantSayDialog(
            onDismissRequest = { confirmCantSay = false },
            onLeaveNote = { state = QuestFormState2.LeaveNote },
            onHideQuest = { onHideQuest(false) },
            onCreateCustomQuest = null
        )
    }
    if (showAccessManager) {
        AccessManagerDialog(
            onDismissRequest = { showAccessManager = false },
            tags = element!!.tags,
            countryInfo = countryInfo
        ) {
            onEdit(UpdateElementTagsAction(element, it.create()))
        }
    }
    if (showConstructionDialog) {
        ConstructionDialog(
            onDismissRequest = { showConstructionDialog = false },
            element = element!!,
            onEdit = { onEdit(UpdateElementTagsAction(element, it)) }
        )
    }
}

@Serializable
private enum class QuestFormState2 {
    Quest,
    LeaveNote,
    SplitWay,
    MoveNode,
    EditTags
}
