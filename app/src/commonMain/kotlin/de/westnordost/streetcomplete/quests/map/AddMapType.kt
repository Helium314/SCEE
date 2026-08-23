package de.westnordost.streetcomplete.quests.map

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddMapType : OsmFilterQuestType<MapType>() {

    override val elementFilter = """
        nodes, ways with
          tourism = information
          and information = map
          and !map_type
    """
    override val changesetComment = "Add map type"
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee
    override val wikiLink = "Key:map_type"
    override val icon = Res.drawable.ic_quest_map_type
    override val title = Res.string.quest_mapType_title
    override val achievements = listOf(EditTypeAchievement.OUTDOORS)

    override fun applyAnswerTo(answer: MapType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["map_type"] = answer.osmValue
    }

    @Composable
    override fun Form(on: (QuestAction<MapType>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            on = on,
            items = MapType.entries,
            itemContent = { ImageWithDescription(painterResource(it.icon), stringResource(it.title), stringResource(it.description)) },
            itemsPerRow = 1,
        )
    }
}
