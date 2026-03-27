package de.westnordost.streetcomplete.quests.building_levels

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.BUILDINGS_WITH_LEVELS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.questPrefix
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_difficult_and_time_consuming
import de.westnordost.streetcomplete.ui.common.dialogs.InfoDialog
import de.westnordost.streetcomplete.resources.*

class AddBuildingLevels : OsmFilterQuestType<BuildingLevels>(), AndroidQuest {

    override val elementFilter = """
        ways, relations with
           building ~ ${BUILDINGS_WITH_LEVELS.joinToString("|")}
           and (
               !building:levels
               ${if (prefs.getBoolean(questPrefix(prefs) + MANDATORY_ROOF_LEVELS, true))
                   "or !roof:levels and !roof:height and roof:shape and roof:shape != flat"
                   else ""
               }
           )
           and !(height and roof:height)
           and !building:min_level
           and !man_made
           and location != underground
           and ruins != yes
    """
    override val changesetComment = "Specify building and roof levels"
    override val wikiLink = "Key:building:levels"
    override val icon = R.drawable.quest_building_levels
    override val title = Res.string.quest_buildingLevels_title2
    override val achievements = listOf(BUILDING)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_difficult_and_time_consuming
    override val hint = Res.string.quest_buildingLevels_hint

    override fun getTitle(tags: Map<String, String>) =
        if (tags.containsKey("building:part")) {
            Res.string.quest_buildingLevels_title_buildingPart2
        } else {
            Res.string.quest_buildingLevels_title2
        }

    override fun createForm() = AddBuildingLevelsForm()

    override fun applyAnswerTo(answer: BuildingLevels, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["building:levels"] = answer.levels.toString()
        answer.roofLevels?.let { tags["roof:levels"] = it.toString() }
    }

    override val hasQuestSettings = true

    @Composable override fun QuestSettings(onDismissRequest: () -> Unit) {
        var showElementSelection by remember { mutableStateOf(false) }
        InfoDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(stringResource(R.string.quest_settings_what_to_edit)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        { prefs.remove(questPrefix(prefs) + MANDATORY_ROOF_LEVELS); onDismissRequest() },
                        Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.quest_settings_building_levels_mandatory_roof))
                    }
                    Button(
                        { prefs.putBoolean(questPrefix(prefs) + MANDATORY_ROOF_LEVELS, false); onDismissRequest() },
                        Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.quest_settings_building_levels_optional_roof))
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

const val MANDATORY_ROOF_LEVELS = "qs_AddBuildingLevels_mandatory_roof_levels"
