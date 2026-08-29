package de.westnordost.streetcomplete.quests.custom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuest
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestController
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestType
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.osmquests.Action
import de.westnordost.streetcomplete.data.osm.osmquests.AddNode
import de.westnordost.streetcomplete.data.osm.osmquests.ExternalAction
import de.westnordost.streetcomplete.osm.toTags
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.main.bottom_sheet.EditTagsForm
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.LocalElement
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.copyTo
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.nameWithoutExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

class CustomQuest(private val customQuestList: CustomQuestList) : ExternalSourceQuestType {

    override val changesetComment = "Edit user-defined list of elements"
    override val wikiLink = "Tags"
    override val icon = Res.drawable.ic_quest_custom
    override val title = Res.string.quest_custom_quest_title
    override val defaultDisabledMessage = Res.string.quest_custom_quest_message

    override val source: String = "custom"

    override suspend fun download(bbox: BoundingBox) = getQuests(bbox)

    override var downloadEnabled = true // it's not actually a download, so no need to ever disable

    override suspend fun upload() { customQuestList.deleteSolved() }

    override fun getQuests(bbox: BoundingBox): Collection<ExternalSourceQuest> = customQuestList.get(bbox)

    override fun get(id: String): ExternalSourceQuest? = customQuestList.getQuest(id)

    override fun onAddedEdit(edit: ElementEdit, id: String) = customQuestList.markSolved(id)

    override fun onDeletedEdit(edit: ElementEdit, id: String?) {
        if (edit.isSynced) return // if it's a real undo, can't undelete the line any more
        id?.let { customQuestList.markSolved(it, false) }
    }

    override fun onSyncedEdit(edit: ElementEdit, id: String?) {
        id?.let { customQuestList.markSolved(it) } // just mark as solved, and bunch-delete in the end
    }

    override fun onSyncEditFailed(edit: ElementEdit, id: String?) {
        id?.let { customQuestList.markSolved(it, false) }
    }

    override suspend fun onUpload(edit: ElementEdit, id: String?): Boolean = true

    override fun deleteQuest(id: String): Boolean = customQuestList.delete(id)

    override fun deleteMetadataOlderThan(timestamp: Long) { }

    override val hasQuestSettings: Boolean = true

    @Composable
    override fun QuestSettings(onDismissRequest: () -> Unit) {
        val scope = rememberCoroutineScope { Dispatchers.IO }
        fun import() {
            scope.launch {
                val file = FileKit.openFilePicker() ?: return@launch
                file.copyTo(customQuestFile)
            }
        }
        fun export() {
            scope.launch {
                val file = FileKit.openFileSaver(customQuestFile.nameWithoutExtension, defaultExtension = customQuestFile.extension) ?: return@launch
                customQuestFile.copyTo(file)
            }
        }
        AlertDialog(
            onDismissRequest = onDismissRequest,
            buttons = {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton({ import() }) {
                        Text(stringResource(Res.string.tree_custom_quest_import))
                    }
                    if (customQuestFile.exists())
                        TextButton({ export() }) {
                            Text(stringResource(Res.string.tree_custom_quest_export))
                        }
                    TextButton(onDismissRequest) { Text(stringResource(Res.string.cancel)) }
                }
            },
            title = { Text(stringResource(Res.string.pref_custom_title)) },
            text = { Text(stringResource(Res.string.tree_custom_quest_import_export_message)) }
        )
    }

    @Composable
    override fun Form(on: (ExternalAction) -> Unit, quest: ExternalSourceQuest, countryInfo: CountryInfo) {
        val questController: ExternalSourceQuestController = koinInject()
        val mapDataSource: MapDataWithEditsSource = koinInject()
        val entry = customQuestList.getEntry(quest.key.id) ?: return
        val tags = if (entry.text.contains("addNode"))
                entry.text.substringAfter("addNode").replace(",", "\n").toTags()
            else null
        val tagsText = tags?.map { "${it.key}=${it.value}" }?.joinToString("\n")
        val pos = entry.position ?: entry.elementKey?.let { mapDataSource.getGeometry(it.type, it.id)?.center }
        val featureDictionary: FeatureDictionary = koinInject()
        @Composable
        fun getTitle(): String {
            val text = entry.text
            return if (text.contains("addNode"))
                stringResource(Res.string.quest_custom_quest_title) + " ${text.substringBefore("addNode")}"
            else
                stringResource(Res.string.quest_custom_quest_title) + " $text"
        }
        @Composable
        fun getSubtitle() =
            tagsText?.let {
                AnnotatedString(stringResource(Res.string.quest_custom_quest_add_node_text, "\n$it"))
            } ?: LocalElement.current?.let { element ->
                nameAndLocationLabel(element, featureDictionary)
            }
        var addNode by remember { mutableStateOf(false) }
        if (!addNode) {
            QuestForm(
                on,
                answers = listOfNotNull(
                    AnswerItem(stringResource(Res.string.quest_custom_quest_remove)) {
                        questController.delete(quest.key)
                        on(Action.TempHideQuest)
                    },
                    if (tagsText != null && pos != null) {
                        AnswerItem(stringResource(Res.string.quest_custom_quest_add_node)) { addNode = true }
                    } else null
                ),
                title = getTitle(),
                subtitle = getSubtitle()
            )
        } else {
            EditTagsForm(
                onConfirmed = {
                    val tags = tags.toMutableMap()
                    it.applyTo(tags)
                    val node = Node(0, pos, tags)
                    on(AddNode(node))
                },
                onDismiss = { addNode = false },
                originalElement = Node(0, pos!!, tags!!)
            )
        }
    }
}
