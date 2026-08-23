package de.westnordost.streetcomplete.quests.custom

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuest
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestController
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestType
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.osmquests.Action
import de.westnordost.streetcomplete.osm.toTags
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.settings.getActivity2
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.LocalElement
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import kotlinx.io.IOException
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import java.io.File

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
        val context = LocalContext.current
        val file = File(context.getExternalFilesDir(null), FILENAME_CUSTOM_QUEST)
        val activity = LocalContext.current.getActivity2()!!
        val customQuestList: CustomQuestList = koinInject()
        val importIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/*"
        }
        val exportIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_TITLE, FILENAME_CUSTOM_QUEST)
            type = "text/*"
        }
        val importFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK || it.data == null)
                return@rememberLauncherForActivityResult
            val uri = it.data?.data ?: return@rememberLauncherForActivityResult
            readFromUriToExternalFile(uri, file.name, activity)
            customQuestList.reload()
            onDismissRequest()
        }
        val exportFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK || it.data == null)
                return@rememberLauncherForActivityResult
            val uri = it.data?.data ?: return@rememberLauncherForActivityResult
            writeFromExternalFileToUri(file.name, uri, activity)
            onDismissRequest()
        }
        AlertDialog(
            onDismissRequest = onDismissRequest,
            buttons = {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton({ importFileLauncher.launch(importIntent) }) {
                        Text(stringResource(Res.string.tree_custom_quest_import))
                    }
                    if (file.exists())
                        TextButton({ exportFileLauncher.launch(exportIntent) }) {
                            Text(stringResource(Res.string.tree_custom_quest_export))
                        }
                    TextButton(onDismissRequest) { Text(stringResource(android.R.string.cancel)) }
                }
            },
            title = { Text(stringResource(Res.string.pref_custom_title)) },
            text = { Text(stringResource(Res.string.tree_custom_quest_import_export_message)) }
        )
    }

    @Composable
    override fun Form(on: (Action) -> Unit, quest: ExternalSourceQuest, countryInfo: CountryInfo) {
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
        QuestForm(
            on,
            answers = listOfNotNull(
                AnswerItem(stringResource(Res.string.quest_custom_quest_remove)) {
                    questController.delete(quest.key)
                },
                if (tagsText != null && pos != null) {
                    AnswerItem(stringResource(Res.string.quest_custom_quest_add_node)) {
/*todo                        val f = CreatePoiFragment.createWithPrefill(tagsText, pos, quest.key)
                        parentFragmentManager.commit {
                            replace(id, f, "bottom_sheet")
                            addToBackStack("bottom_sheet")
                        }
                        (activity as? MainActivity)?.offsetPos(p)
*/                    }
                } else null
            ),
            title = getTitle(),
            subtitle = getSubtitle()
        )
    }
}

fun readFromUriToExternalFile(uri: Uri, filename: String, activity: Activity) {
    try {
        activity.contentResolver?.openInputStream(uri)?.use { it.bufferedReader().use { reader ->
            File(activity.getExternalFilesDir(null), filename).writeText(reader.readText())
        } }
    } catch (_: IOException) {
        AlertDialog.Builder(activity)
            .setMessage(R.string.pref_save_file_error)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}

fun writeFromExternalFileToUri(filename: String, uri: Uri, activity: Activity) {
    try {
        activity.contentResolver?.openOutputStream(uri)?.use { it.bufferedWriter().use { writer ->
            writer.write(File(activity.getExternalFilesDir(null), filename).readText())
        } }
    } catch (_: IOException) {
        AlertDialog.Builder(activity)
            .setMessage(R.string.pref_save_file_error)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}
