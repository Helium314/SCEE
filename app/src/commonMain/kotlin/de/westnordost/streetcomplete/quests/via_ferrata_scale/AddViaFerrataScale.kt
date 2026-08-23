package de.westnordost.streetcomplete.quests.via_ferrata_scale

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
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddViaFerrataScale : OsmFilterQuestType<ViaFerrataScale>() {

    override val elementFilter = """
        ways with
          highway = via_ferrata
          and !via_ferrata_scale
    """
    override val changesetComment = "Specify Via Ferrata Grade Scale"
    override val wikiLink = "Key:via_ferrata_scale"
    override val icon = Res.drawable.ic_quest_via_ferrata_scale
    override val title = Res.string.quest_viaFerrataScale_title
    override val defaultDisabledMessage = Res.string.default_disabled_msg_viaFerrataScale

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("ways with highway = via_ferrata")

    @Composable
    override fun Form(on: (QuestAction<ViaFerrataScale>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            on = on,
            items = ViaFerrataScale.entries,
            itemContent = {
                ImageWithDescription(
                    painterResource(it.imageRes),
                    stringResource(it.titleRes),
                    stringResource(it.descriptionRes)
                )
            },
            itemsPerRow = 1
        )
    }

    override fun applyAnswerTo(answer: ViaFerrataScale, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["via_ferrata_scale"] = answer.osmValue
    }
}
