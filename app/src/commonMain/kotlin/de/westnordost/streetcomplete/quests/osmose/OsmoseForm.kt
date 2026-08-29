package de.westnordost.streetcomplete.quests.osmose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuest
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestController
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.osmquests.Action
import de.westnordost.streetcomplete.data.osm.osmquests.EditElement
import de.westnordost.streetcomplete.data.osm.osmquests.ExternalAction
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.quests.questPrefix
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.main.bottom_sheet.EditTagsForm
import de.westnordost.streetcomplete.ui.common.dialogs.AlertDialog
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.LocalElement
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun OsmoseForm(on: (ExternalAction) -> Unit, quest: ExternalSourceQuest) {
    val osmoseDao: OsmoseDao = koinInject()
    val questController: ExternalSourceQuestController = koinInject()
    val issue = remember { osmoseDao.getIssue(quest.key.id) }
    if (issue == null) {
        questController.delete(quest.key)
        on(Action.TempHideQuest)
        return
    }
    //if (issue.elements.size > 1) viewLifecycleScope.launch { highlightElements() } todo
    val featureDictionary: FeatureDictionary = koinInject()
    var confirmFalsePositive by remember { mutableStateOf(false) }
    var editTags by remember { mutableStateOf(false) }
    var elementToEdit by remember { mutableStateOf<ElementKey?>(null) }
    var showIgnoreDialog by remember { mutableStateOf(false) }
    if (elementToEdit != null) {
        val mapDataSource: MapDataWithEditsSource = koinInject()
        val element = mapDataSource.get(elementToEdit!!.type, elementToEdit!!.id)
        if (element == null) elementToEdit = null
        else EditTagsForm(
            { on(EditElement(UpdateElementTagsAction(element, it))); osmoseDao.setDone(issue.uuid) },
            { elementToEdit = null },
            element
        )
    }
    if (elementToEdit == null) {
        QuestForm(
            on,
            answers = listOfNotNull(
                if (issue.elements.isEmpty()) null else
                    AnswerItem(stringResource(Res.string.quest_generic_answer_show_edit_tags)) {
                        editTags = true
                    },
                AnswerItem(stringResource(Res.string.quest_osmose_false_positive)) {
                    confirmFalsePositive = true
                }
            ),
            otherAnswers = { listOf(
                AnswerItem(stringResource(Res.string.quest_osmose_hide_type)) { showIgnoreDialog = true },
                AnswerItem(stringResource(Res.string.quest_osmose_delete_this_issue)) {
                    questController.delete(quest.key)
                    on(Action.TempHideQuest)
                },
            ) },
            title = stringResource(Res.string.quest_osmose_title) + " ${issue.title}",
            subtitle = LocalElement.current?.let { element ->
                nameAndLocationLabel(element, featureDictionary)
            }
        ) {
            Text(stringResource(Res.string.quest_osmose_message_for_element, "${issue.item}/${issue.itemClass}", issue.subtitle))
            if (confirmFalsePositive) {
                ConfirmationDialog(
                    { confirmFalsePositive = false },
                    // todo: do some kind of edit, so it can be undone? the edit could be deleted on upload (see also ExternalSourceModule commented stuff)
                    { osmoseDao.setAsFalsePositive(issue.uuid); on(Action.TempHideQuest) },
                    title = { Text(stringResource(Res.string.quest_osmose_false_positive)) },
                    confirmButtonText = stringResource(Res.string.quest_generic_confirmation_yes),
                    text = { Text(stringResource(Res.string.quest_osmose_no_undo)) }
                )
            }

        }
    }
    if (editTags) {
        if (issue.elements.size == 1) {
            elementToEdit = issue.elements.single()
            editTags = false
        } else {
            AlertDialog(
                onDismissRequest = { editTags = false },
                buttonRow = { TextButton({ editTags = false }) { Text(stringResource(Res.string.cancel)) } },
                title = { Text(stringResource(Res.string.quest_osmose_select_element)) },
                text = {
                    Column {
                        issue.elements.forEach {
                            TextButton({ elementToEdit = it; editTags = false }, Modifier.fillMaxWidth()) { Text("${it.type} ${it.id}") }
                        }
                    }
                }
            )
        }
    }
    if (showIgnoreDialog) {
        val prefs: Preferences = koinInject()
        fun addToIgnoreList(item: String) {
            val types = prefs.getString(questPrefix(prefs) + PREF_OSMOSE_ITEMS, OSMOSE_DEFAULT_IGNORED_ITEMS)
                .split("§§")
                .mapNotNull { if (it.isNotBlank()) it.trim() else null }
                .toMutableSet()
            types.add(item)
            prefs.putString(questPrefix(prefs) + PREF_OSMOSE_ITEMS,types.sorted().joinToString("§§"))
            osmoseDao.reloadIgnoredItems()
            questController.invalidate()
            on(Action.TempHideQuest)
        }
        AlertDialog(
            onDismissRequest = { showIgnoreDialog = false },
            buttonRow = { TextButton({ showIgnoreDialog = false }) { Text(stringResource(Res.string.cancel)) } },
            title = { Text(stringResource(Res.string.quest_osmose_hide_type)) },
            text = {
                Column {
                    TextButton({ addToIgnoreList(issue.item.toString()) }) { Text("item: ${issue.item}") }
                    TextButton({ addToIgnoreList("${issue.item}/${issue.itemClass}") }) { Text("item/class: ${issue.item}/${issue.itemClass}") }
                    if (issue.subtitle.isNotBlank())
                        TextButton({ addToIgnoreList(issue.subtitle) }) { Text("subtitle: ${issue.subtitle}") }
                }
            }
        )
    }
}

/*
    private fun highlightElements() {
        val issue = issue ?: return
        val elementsAndGeometry = issue.elements.mapNotNull { mapDataSource.get(it.type, it.id) }.mapNotNull { e -> mapDataSource.getGeometry(e.type, e.id)?.let { e to it } }

        if (prefs.getBoolean(Prefs.SHOW_WAY_DIRECTION, false) && elementsAndGeometry.any { it.second is ElementPolylinesGeometry }) {
            // show geometry containing way direction together with normal one. not nice looking, but:
            //  normal one contains way labels, which are necessary for editing
            //  this here contains the arrows
            // and adding arrows to "normal" highlighted ways in special cases only is maybe work for later
            val mapFragment = (activity as? MainActivity)?.supportFragmentManager?.fragments?.filterIsInstance<MainMapFragment>()?.singleOrNull()
            mapFragment?.highlightGeometries(elementsAndGeometry.map { it.second })
        }

        val showsGeometryMarkersListener = activity as? ShowsGeometryMarkers ?: return
        showsGeometryMarkersListener.putMarkersForCurrentHighlighting(elementsAndGeometry.map {
            Marker(it.second, null, "${it.first.type} ${it.first.id}")
        })
    }
 */
