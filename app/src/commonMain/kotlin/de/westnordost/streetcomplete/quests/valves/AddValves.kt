package de.westnordost.streetcomplete.quests.valves

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.tracktype.Tracktype
import de.westnordost.streetcomplete.quests.tracktype.icon
import de.westnordost.streetcomplete.quests.tracktype.title
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_ee
import de.westnordost.streetcomplete.resources.ic_quest_valve
import de.westnordost.streetcomplete.resources.quest_valves_title
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.quest.ItemsSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddValves : OsmFilterQuestType<Set<Valves>>() {

    override val elementFilter = """
        nodes, ways with
          (compressed_air = yes
          or service:bicycle:pump = yes
          or amenity = compressed_air)
          and access !~ private|no
          and !valves
    """
    override val changesetComment = "Specify valves types for air pumps or compressed air"
    override val wikiLink = "Key:valves"
    override val icon = Res.drawable.ic_quest_valve
    override val title = Res.string.quest_valves_title
    override val achievements = listOf(BICYCLIST)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    @Composable
    override fun Form(on: (QuestAction<Set<Valves>>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemsSelectQuestForm(
            on = on,
            items = Valves.entries,
            itemContent = { ImageWithLabel(painterResource(it.iconRes), stringResource(it.titleRes)) },
        )
    }

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with amenity = compressed_air or service:bicycle:pump = yes or compressed_air = yes")

    override fun applyAnswerTo(answer: Set<Valves>, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["valves"] = answer.joinToString(";") { it.osmValue }
    }
}
