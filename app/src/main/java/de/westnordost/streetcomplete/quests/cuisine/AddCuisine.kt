package de.westnordost.streetcomplete.quests.cuisine

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.osm.IS_SHOP_OR_DISUSED_SHOP_EXPRESSION

class AddCuisine : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes, ways with
        (
            amenity ~ restaurant|fast_food
            or amenity = pub and food = yes
        )
        and name and !cuisine
    """
    override val changesetComment = "Add cuisine"
    override val wikiLink = "Key:cuisine"
    override val icon = R.drawable.ic_quest_restaurant_vegan
    override val isReplaceShopEnabled = true
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside

    override fun getTitle(tags: Map<String, String>) = R.string.quest_cuisine_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter(IS_SHOP_OR_DISUSED_SHOP_EXPRESSION)

    override fun createForm() = AddCuisineForm()

    override fun applyAnswerTo(answer: String, tags: Tags, timestampEdited: Long) {
        tags["cuisine"] = answer
    }
}
