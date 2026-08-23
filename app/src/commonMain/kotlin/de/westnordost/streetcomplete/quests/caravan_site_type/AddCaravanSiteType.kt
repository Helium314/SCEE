package de.westnordost.streetcomplete.quests.caravan_site_type

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
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

class AddCaravanSiteType : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes, ways, relations with
            tourism = caravan_site and (
            !caravan_site:type
        )
    """
    override val changesetComment = "Add caravan site type info"
    override val defaultDisabledMessage = Res.string.default_disabled_msg_caravanSiteType
    override val wikiLink = "Key:caravan_site:type"
    override val icon = Res.drawable.ic_quest_caravan_site
    override val title = Res.string.quest_caravanSiteType_title
    override val achievements = listOf(OUTDOORS)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways, relations with tourism ~ caravan_site|camp_site")

    @Composable
    override fun Form(on: (QuestAction<String>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        val items = listOf(
            "village",
            "town",
            "river",
            "lake",
            "parking_lot",
            "harbour",
            "winery",
            "camp_site",
            "museum",
            "restaurant",
            "farm",
            "beach",
            "supermarket",
        )
        @Composable
        fun text(item: String) = stringResource(when (item) {
            "village" -> Res.string.quest_caravanSiteType_village
            "town" -> Res.string.quest_caravanSiteType_town
            "river" -> Res.string.quest_caravanSiteType_river
            "lake" -> Res.string.quest_caravanSiteType_lake
            "parking_lot" -> Res.string.quest_caravanSiteType_parking_lot
            "harbour" -> Res.string.quest_caravanSiteType_harbour
            "winery" -> Res.string.quest_caravanSiteType_winery
            "camp_site" -> Res.string.quest_caravanSiteType_camp_site
            "museum" -> Res.string.quest_caravanSiteType_museum
            "restaurant" -> Res.string.quest_caravanSiteType_restaurant
            "farm" -> Res.string.quest_caravanSiteType_farm
            "beach" -> Res.string.quest_caravanSiteType_beach
            "supermarket" -> Res.string.quest_caravanSiteType_supermarket
            else -> null
        }!!)

        ItemSelectQuestForm(
            on = on,
            items = items,
            itemContent = { Text(text(it)) }
        )
    }

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["caravan_site:type"] = answer
    }
}
