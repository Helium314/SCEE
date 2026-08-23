package de.westnordost.streetcomplete.quests.street_cabinet

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.quest.LocalQuestType
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddStreetCabinetType : OsmFilterQuestType<StreetCabinetType>() {

    override val elementFilter = """
        nodes, ways with
          man_made = street_cabinet
          and !street_cabinet
          and !utility
    """
    override val changesetComment = "Add street cabinet type"
    override val wikiLink = "Tag:man_made=street_cabinet"
    override val icon = Res.drawable.ic_quest_street_cabinet
    override val title = Res.string.quest_street_cabinet_type_title
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("""
            nodes, ways with
             (
                 man_made = street_cabinet
                 or building ~ service|transformer_tower
             )
        """)

    @Composable
    override fun Form(on: (QuestAction<StreetCabinetType>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            on = on,
            items = StreetCabinetType.entries,
            itemContent = {
                ImageWithLabel(
                    painterResource(it.icon),
                    stringResource(it.title)
                )
            },
            itemsPerRow = 4,
            title = if (element.tags.containsKey("operator"))
                    stringResource(LocalQuestType.current!!.title) + " (${element.tags["operator"]})"
                else stringResource(LocalQuestType.current!!.title)
        )
    }

    override fun applyAnswerTo(answer: StreetCabinetType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags[answer.osmKey] = answer.osmValue
    }
}
