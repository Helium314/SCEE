package de.westnordost.streetcomplete.quests.building_material

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_difficult_and_time_consuming
import de.westnordost.streetcomplete.resources.ic_quest_building_material
import de.westnordost.streetcomplete.resources.quest_buildingColour_title
import de.westnordost.streetcomplete.resources.quest_buildingMaterial_title
import de.westnordost.streetcomplete.resources.quest_buildingPartColour_title
import de.westnordost.streetcomplete.resources.quest_buildingPartMaterial_title
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBuildingMaterial : OsmFilterQuestType<BuildingMaterial>() {

    override val elementFilter = """
        ways, relations with
          ((building and building !~ no|construction|roof|carport)
          or (building:part and building:part !~ no|construction|roof|carport))
          and !building:material
          and indoor != no
          and wall != no
    """
    override val changesetComment = "Specify building material"
    override val wikiLink = "Key:building:material"
    override val icon = Res.drawable.ic_quest_building_material
    override val title = Res.string.quest_buildingMaterial_title
    override val defaultDisabledMessage = Res.string.default_disabled_msg_difficult_and_time_consuming

    @Composable
    override fun Form(on: (QuestAction<BuildingMaterial>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            on = on,
            items = BuildingMaterial.entries,
            itemContent = { ImageWithLabel(painterResource(it.imageRes), stringResource(it.titleRes)) },
            title = stringResource(if (element.tags.containsKey("building:part")) Res.string.quest_buildingPartMaterial_title
            else Res.string.quest_buildingMaterial_title)
        )
    }

    override fun applyAnswerTo(
        answer: BuildingMaterial,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long,
    ) {
        tags["building:material"] = answer.osmValue
    }
}
