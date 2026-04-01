package de.westnordost.streetcomplete.quests.map

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_ee
import de.westnordost.streetcomplete.resources.quest_mapSize_title

class AddMapSize : OsmFilterQuestType<String>(), AndroidQuest {

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
    override val icon = R.drawable.ic_quest_map_size
    override val achievements = listOf(EditTypeAchievement.OUTDOORS)

    override fun createForm() = AddMapSizeForm()

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["map_size"] = answer
    }
}

