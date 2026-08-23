package de.westnordost.streetcomplete.quests.lamp_type

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.common.quest.RadioGroupQuestForm
import org.jetbrains.compose.resources.stringResource

class AddLampType : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes with
          highway = street_lamp
          and (!lamp_type or lamp_type ~ electric|floodlight|sodium|solar_lamp)
          and (!light:method or light:method ~ electric|discharge|sodium)
    """
    override val changesetComment = "Add lamp type"
    override val defaultDisabledMessage = Res.string.quest_lampType_disabled_msg
    override val wikiLink = "Key:lamp_type"
    override val title = Res.string.quest_lampType_title
    override val icon = Res.drawable.ic_quest_lamp_type
    override val achievements = listOf(EditTypeAchievement.CITIZEN)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes with highway = street_lamp")

    @Composable
    override fun Form(on: (QuestAction<String>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        val items = listOf(
            "led",
            "high_pressure_sodium",
            "low_pressure_sodium",
            "gaslight",
            "fluorescent",
            "incandescent",
            "metal-halide",
            "mercury",
            "halogen",
        )
        @Composable
        fun text(item: String) = stringResource(when (item) {
            "led" -> Res.string.quest_lampType_led
            "high_pressure_sodium" -> Res.string.quest_lampType_highPressureSodium
            "low_pressure_sodium" -> Res.string.quest_lampType_lowPressureSodium
            "gaslight" -> Res.string.quest_lampType_gaslight
            "fluorescent" -> Res.string.quest_lampType_fluorescent
            "incandescent" -> Res.string.quest_lampType_incandescent
            "metal-halide" -> Res.string.quest_lampType_metalHalide
            "mercury" -> Res.string.quest_lampType_mercury
            "halogen" -> Res.string.quest_lampType_halogen
            else -> null
        }!!)

        RadioGroupQuestForm(
            on = on,
            items = items,
            itemContent = { Text(text(it)) }
        )
    }

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["lamp_type"] = answer
        if (tags["light:method"] in listOf("electric", "discharge", "sodium")) {
            tags.remove("light:method")
        }
    }
}
