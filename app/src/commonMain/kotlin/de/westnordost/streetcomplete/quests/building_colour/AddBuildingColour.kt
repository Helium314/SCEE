package de.westnordost.streetcomplete.quests.building_colour

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBuildingColour : OsmFilterQuestType<BuildingColour>() {

    override val elementFilter = """
        ways, relations with
          ((building and building !~ no|construction|roof|carport)
          or (building:part and building:part !~ no|construction|roof|carport))
          and !building:colour
          and (!indoor or indoor = no)
          and wall !~ no
          and location != underground
    """
    override val changesetComment = "Specify building colour"
    override val wikiLink = "Key:building:colour"
    override val title = Res.string.quest_buildingColour_title
    override val icon = Res.drawable.ic_quest_building_colour
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    @Composable
    override fun Form(on: (QuestAction<BuildingColour>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            on = on,
            items = BuildingColour.entries,
            itemContent = { ImageWithLabel(painterResource(Res.drawable.ic_building_colour), it.title, colorFilter = it.colorFilter()) },
            itemsPerRow = 4,
            title = stringResource(if (element.tags.containsKey("building:part")) Res.string.quest_buildingPartColour_title
                else Res.string.quest_buildingColour_title)
        )
    }

    override fun applyAnswerTo(
        answer: BuildingColour,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long,
    ) {
        tags["building:colour"] = answer.osmValue
    }
}

