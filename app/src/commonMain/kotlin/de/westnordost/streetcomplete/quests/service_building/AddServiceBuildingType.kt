package de.westnordost.streetcomplete.quests.service_building

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.GroupedItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.quest.LocalQuestType
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddServiceBuildingType : OsmFilterQuestType<ServiceBuildingType>() {

    override val elementFilter = """
        ways, relations with
          building ~ service|transformer_tower
          and !power and !disused:power and !abandoned:power and !was:power and !construction:power
          and !service and !disused:service and !abandoned:service and !was:service and !construction:service
          and !man_made and !disused:man_made and !abandoned:man_made and !was:man_made and !construction:man_made
          and !substation and !disused:substation and !abandoned:substation and !was:substation and !construction:substation
          and !pipeline and !disused:pipeline and !abandoned:pipeline and !was:pipeline and !construction:pipeline
          and !utility and !disused:utility and !abandoned:utility and !was:utility and !construction:utility
          and !railway and !disused:railway and !abandoned:railway and !was:railway and !construction:railway
          and disused != yes and abandoned != yes and !construction
    """
    override val changesetComment = "Add service building type"
    override val wikiLink = "Tag:building=service"
    override val icon = Res.drawable.ic_quest_service_building
    override val title = Res.string.quest_service_building_type_title
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    @Composable
    override fun Form(on: (QuestAction<ServiceBuildingType>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        GroupedItemSelectQuestForm(
            on = on,
            groups = ServiceBuildingTypeCategory.entries,
            topItems = listOf(
                ServiceBuildingType.MINOR_SUBSTATION,
                ServiceBuildingType.GAS_PRESSURE_REGULATION,
                ServiceBuildingType.VENTILATION_SHAFT,
                ServiceBuildingType.WATER_WELL,
                ServiceBuildingType.HEATING,
            ),
            groupContent = {
                ImageWithDescription(
                    painter = painterResource(it.iconRes),
                    title = stringResource(it.titleRes),
                    description = null,
                    imageSize = DpSize(48.dp, 48.dp)
                )
            },
            itemContent = {
                ImageWithDescription(
                    painter = painterResource(it.iconRes),
                    title = stringResource(it.titleRes),
                    description = it.descriptionRes?.let { stringResource(it) },
                    imageSize = DpSize(48.dp, 48.dp),
                )
            },
            favoriteKey = "AddServiceBuildingTypeForm",
            title = if (element.tags.containsKey("operator"))
                    stringResource(LocalQuestType.current!!.title) + " (${element.tags["operator"]})"
                else stringResource(LocalQuestType.current!!.title),
            otherAnswers = { listOf(AnswerItem(stringResource(Res.string.quest_disused)) { on(Answer(ServiceBuildingType.DISUSED)) }) }
        )
    }

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("ways, relations with building ~ service|transformer_tower or power")

    override fun applyAnswerTo(answer: ServiceBuildingType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.tags.forEach { tags[it.first] = it.second }
        if (answer == ServiceBuildingType.VENTILATION_SHAFT || answer == ServiceBuildingType.RAILWAY_VENTILATION_SHAFT)
            tags.remove("building") // see https://wiki.openstreetmap.org/wiki/Tag:man_made%3Dventilation_shaft
    }
}
