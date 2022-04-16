package de.westnordost.streetcomplete.quests.seating

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.IS_SHOP_OR_DISUSED_SHOP_EXPRESSION
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddOutdoorSeatingType : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes, ways with
          outdoor_seating = yes
    """
    override val changesetComment = "Add outdoor seating info"
    override val defaultDisabledMessage = R.string.default_disabled_msg_summer_outdoor_seating
    override val wikiLink = "Key:outdoor_seating"
    override val icon = R.drawable.ic_quest_seating
    override val isReplaceShopEnabled = true
    override val questTypeAchievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_outdoor_seating_name_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter(IS_SHOP_OR_DISUSED_SHOP_EXPRESSION)

    override fun createForm() = AddOutdoorSeatingTypeForm()

    override fun applyAnswerTo(answer: String, tags: Tags, timestampEdited: Long) {
        tags["outdoor_seating"] = answer
    }
}
