package de.westnordost.streetcomplete.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuest
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.data.visiblequests.LevelFilter
import de.westnordost.streetcomplete.osm.level.LevelTypes
import de.westnordost.streetcomplete.osm.level.parseSelectableLevels
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.cancel
import de.westnordost.streetcomplete.resources.level_filter_enable
import de.westnordost.streetcomplete.resources.level_filter_message
import de.westnordost.streetcomplete.resources.level_filter_title
import de.westnordost.streetcomplete.resources.ok
import de.westnordost.streetcomplete.screens.main.map.maplibre.CameraPosition
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import kotlin.math.ceil
import kotlin.math.floor

@Composable fun LevelFilterDialog(
    onDismissRequest: () -> Unit,
    camera: CameraPosition?
) {
    val levelFilter: LevelFilter = koinInject()
    val prefs: Preferences = koinInject()
    val mapDataSource: MapDataWithEditsSource = koinInject()
    val visibleQuestsSource: VisibleQuestsSource = koinInject()

    val levelTags = prefs.getString(Prefs.ALLOWED_LEVEL_TAGS, "level,repeat_on,level:ref").split(",")
    val allowedLevelTypes = LevelTypes.entries.filter { levelTags.contains(it.tag) }
    var levelText by remember { mutableStateOf(TextFieldValue(prefs.getString(Prefs.ALLOWED_LEVEL, ""))) }

    var level by remember { mutableStateOf(levelTags.contains("level")) }
    var repeatOn by remember { mutableStateOf(levelTags.contains("repeat_on")) }
    var levelRef by remember { mutableStateOf(levelTags.contains("level:ref")) }
    var addrFloor by remember { mutableStateOf(levelTags.contains("addr:floor")) }
    var enabled by remember { mutableStateOf(levelFilter.isEnabled) }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = { TextButton(onClick = {
            val levelTagList = mutableListOf<String>()
            if (level) levelTagList.add("level")
            if (repeatOn) levelTagList.add("repeat_on")
            if (levelRef) levelTagList.add("level:ref")
            if (addrFloor) levelTagList.add("addr:floor")
            prefs.putString(Prefs.ALLOWED_LEVEL_TAGS, levelTagList.joinToString(","))
            prefs.putString(Prefs.ALLOWED_LEVEL, levelText.text)
            if (levelFilter.isEnabled != enabled)
                levelFilter.isEnabled = enabled
            else levelFilter.reload()
            onDismissRequest()
        } ) {
            Text(stringResource(Res.string.ok))
        } },
        dismissButton = { TextButton(onClick = onDismissRequest ) {
            Text(stringResource(Res.string.cancel))
        } },
        title = { Text(stringResource(Res.string.level_filter_title)) },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { level = !level }) {
                    Checkbox(level, { level = it })
                    Text("level")
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { repeatOn = !repeatOn }) {
                    Checkbox(repeatOn, { repeatOn = it })
                    Text("repeat_on")
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { levelRef = !levelRef }) {
                    Checkbox(levelRef, { levelRef = it })
                    Text("level:ref")
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { addrFloor = !addrFloor }) {
                    Checkbox(addrFloor, { addrFloor = it })
                    Text("addr:floor")
                }
                Text(stringResource(Res.string.level_filter_message))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button({
                        val selectableLevels = getLevelsInView(camera?.position?.enclosingBoundingBox(50.0), allowedLevelTypes, visibleQuestsSource, mapDataSource)
                        val oldText = levelText.text
                        val currentLevel = "[\\d.+-]+".toRegex().find(oldText)?.value
                        val currentLevelNumber = currentLevel?.toDoubleOrNull()
                        val newLevel = if (currentLevelNumber == null) {
                            selectableLevels.find { it >= 0 } ?: selectableLevels.firstOrNull() ?: 0.0
                        } else {
                            val nextInt = floor(currentLevelNumber + 1.0)
                            selectableLevels.find { it > currentLevelNumber && it < nextInt } ?: nextInt
                        }
                        levelText = TextFieldValue(oldText.replace(currentLevel ?: oldText, newLevel.toNiceString()))
                    }) { Text("+") }
                    Button({
                        val selectableLevels = getLevelsInView(camera?.position?.enclosingBoundingBox(50.0), allowedLevelTypes, visibleQuestsSource, mapDataSource)
                        val oldText = levelText.text
                        val currentLevel = "[\\d.+-]+".toRegex().find(oldText)?.value
                        val currentLevelNumber = currentLevel?.toDoubleOrNull()
                        val newLevel = if (currentLevelNumber == null) {
                            selectableLevels.findLast { it <= 0 } ?: selectableLevels.firstOrNull() ?: 0.0
                        } else {
                            val prevInt = ceil(currentLevelNumber - 1.0)
                            selectableLevels.findLast { it < currentLevelNumber && it > prevInt } ?: prevInt
                        }
                        levelText = TextFieldValue(oldText.replace(currentLevel ?: oldText, newLevel.toNiceString()))
                    }) { Text("-") }
                    TextField(levelText, { levelText = it })
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { enabled = !enabled }) {
                    Text(stringResource(Res.string.level_filter_enable))
                    Switch(enabled, { enabled = it })
                }
            }
        }
    )
}

private fun getLevelsInView(displayedArea: BoundingBox?, allowed: List<LevelTypes>, visibleQuestsSource: VisibleQuestsSource, mapDataSource: MapDataWithEditsSource): List<Double> {
    val tags = if (displayedArea != null) {
        visibleQuestsSource.getAll(displayedArea).mapNotNull {
            when (it) {
                is OsmQuest -> mapDataSource.get(it.elementType, it.elementId)
                is ExternalSourceQuest -> it.elementKey?.let { mapDataSource.get(it.type, it.id) }
                else -> null
            }?.tags
        }
    } else emptyList()
    return parseSelectableLevels(tags, allowed)
}

private fun Double.toNiceString(): String {
    if (toInt().toDouble() == this) return toInt().toString()
    return toString()
}
