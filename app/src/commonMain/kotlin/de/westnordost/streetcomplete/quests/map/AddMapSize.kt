package de.westnordost.streetcomplete.quests.map

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
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
import org.jetbrains.compose.resources.stringResource

class AddMapSize : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes, ways with
          tourism = information
          and information = map
          and !map_size
    """
    override val changesetComment = "Add what area a map covers"
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee
    override val wikiLink = "Key:map_size"
    override val title = Res.string.quest_mapSize_title
    override val icon = Res.drawable.ic_quest_map_size
    override val achievements = listOf(EditTypeAchievement.OUTDOORS)


    @Composable
    override fun Form(on: (QuestAction<String>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        val items = listOf(
            "site",
            "city",
            "landscape",
            "region",
        )
        @Composable
        fun text(item: String) = stringResource(when (item) {
            "site" -> Res.string.quest_mapSize_site
            "city" -> Res.string.quest_mapSize_city
            "landscape" -> Res.string.quest_mapSize_landscape
            "region" -> Res.string.quest_mapSize_region
            else -> null
        }!!)

        ItemSelectQuestForm(
            on = on,
            items = items,
            itemContent = { Text(text(it)) }
        )
    }

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["map_size"] = answer
    }
}

