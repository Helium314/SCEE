package de.westnordost.streetcomplete.quests.tree

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.custom.readFromUriToExternalFile
import de.westnordost.streetcomplete.quests.custom.writeFromExternalFileToUri
import de.westnordost.streetcomplete.util.ktx.getActivity
import java.io.File
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_tree_disabled_msg
import de.westnordost.streetcomplete.resources.quest_tree_genus_title
import de.westnordost.streetcomplete.ui.common.dialogs.InfoDialog

class AddTreeGenus : OsmFilterQuestType<TreeAnswer>(), AndroidQuest {

    override val elementFilter = """
        nodes with
          natural = tree
          and !genus and !species and !taxon
          and !~"genus:.*" and !~"species:.*" and !~"taxon:.*"
    """
    override val changesetComment = "Add tree genus/species"
    override val defaultDisabledMessage = Res.string.quest_tree_disabled_msg
    override val wikiLink = "Key:genus"
    override val icon = R.drawable.quest_tree
    override val title = Res.string.quest_tree_genus_title

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes with natural = tree")

    override fun createForm() = AddTreeGenusForm()

    override val isDeleteElementEnabled = true

    override fun applyAnswerTo(answer: TreeAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is NotTreeButStump -> tags["natural"] = "tree_stump"
            is Tree -> {
                if (answer.isSpecies)
                    tags["species"] = answer.name
                else
                    tags["genus"] = answer.name
            }
        }
    }

    @Composable
    override fun QuestSettings(onDismissRequest: () -> Unit) {
        val context = LocalContext.current
        var showElementSelection by remember { mutableStateOf(false) }
        val file = File(context.getExternalFilesDir(null), FILENAME_TREES)
        val activity = LocalContext.current.getActivity()!!
        val importIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/*"
        }
        val exportIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_TITLE, FILENAME_TREES)
            type = "text/*"
        }
        val importFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK || it.data == null)
                return@rememberLauncherForActivityResult
            val uri = it.data?.data ?: return@rememberLauncherForActivityResult
            readFromUriToExternalFile(uri, file.name, activity)
            onDismissRequest()
        }
        val exportFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK || it.data == null)
                return@rememberLauncherForActivityResult
            val uri = it.data?.data ?: return@rememberLauncherForActivityResult
            writeFromExternalFileToUri(file.name, uri, activity)
            onDismissRequest()
        }
        InfoDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(stringResource(R.string.pref_trees_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.tree_custom_quest_import_export_message))
                    Button({ importFileLauncher.launch(importIntent) }, Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.tree_custom_quest_import))
                    }
                    if (file.exists())
                        Button({ exportFileLauncher.launch(exportIntent) }, Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.tree_custom_quest_export))
                        }
                    Button({ showElementSelection = true }, Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.element_selection_button))
                    }
                }
            }
        )
        if (showElementSelection)
            super.QuestSettings(onDismissRequest)
    }
}
