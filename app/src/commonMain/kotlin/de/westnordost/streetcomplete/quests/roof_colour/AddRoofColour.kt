package de.westnordost.streetcomplete.quests.roof_colour

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.building_colour.colorFilter
import de.westnordost.streetcomplete.quests.building_colour.title
import de.westnordost.streetcomplete.quests.roof_shape.RoofShape
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource

class AddRoofColour : OsmFilterQuestType<RoofColour>() {

    override val elementFilter = """
        ways, relations with
          roof:shape
          and roof:shape != flat
          and !roof:colour
          and building
          and building !~ no|construction
          and location != underground
          and ruins != yes
    """
    override val changesetComment = "Specify roof colour"
    override val wikiLink = "Key:roof:colour"
    override val icon = Res.drawable.ic_quest_roof_colour
    override val title = Res.string.quest_roofColour_title
    override val achievements = listOf(BUILDING)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_roof

    @Composable
    override fun Form(on: (QuestAction<RoofColour>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        val shape = element.tags["roof:shape"]
        val iconRes = RoofShape.entries.firstOrNull { it.osmValue == shape }?.colorIconResId ?: Res.drawable.ic_roof_colour_gabled
        ItemSelectQuestForm(
            on = on,
            items = RoofColour.entries,
            itemContent = { ImageWithLabel(painterResource(iconRes), it.title, colorFilter = it.colorFilter()) },
            itemsPerRow = 4
        )
    }

    override fun applyAnswerTo(
        answer: RoofColour,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long,
    ) {
        tags["roof:colour"] = answer.osmValue
    }
}

