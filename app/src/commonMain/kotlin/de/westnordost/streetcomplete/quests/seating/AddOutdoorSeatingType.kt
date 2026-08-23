package de.westnordost.streetcomplete.quests.seating

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.places.isPlace
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.common.quest.RadioGroupQuestForm
import org.jetbrains.compose.resources.stringResource

class AddOutdoorSeatingType : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes, ways with
          outdoor_seating = yes
    """
    override val changesetComment = "Add outdoor seating info"
    override val defaultDisabledMessage = Res.string.default_disabled_msg_seasonal
    override val wikiLink = "Key:outdoor_seating"
    override val icon = Res.drawable.ic_quest_seating_type
    override val title = Res.string.quest_outdoor_seating_name_title
    override val achievements = listOf(EditTypeAchievement.CITIZEN)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.asSequence().filter { it.isPlace() }

    @Composable
    override fun Form(on: (QuestAction<String>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        val items = listOf(
            "parklet",
            "pedestrian_zone",
            "street",
            "sidewalk",
            "patio",
            "terrace",
            "balcony",
            "veranda",
            "roof",
            "garden",
            "beach",
        )
        fun title(item: String) = when (item) {
            "parklet" -> Res.string.quest_seating_parklet
            "pedestrian_zone" -> Res.string.quest_seating_pedestrian_zone
            "street" -> Res.string.quest_seating_street
            "sidewalk" -> Res.string.quest_seating_sidewalk
            "patio" -> Res.string.quest_seating_patio
            "terrace" -> Res.string.quest_seating_terrace
            "balcony" -> Res.string.quest_seating_balcony
            "veranda" -> Res.string.quest_seating_veranda
            "roof" -> Res.string.quest_seating_roof
            "garden" -> Res.string.quest_seating_garden
            "beach" -> Res.string.quest_seating_beach
            else -> null
        }
        RadioGroupQuestForm(
            on = on,
            items = items,
            itemContent = { Text(stringResource(title(it)!!)) }
        )
    }

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["outdoor_seating"] = answer
    }
}

