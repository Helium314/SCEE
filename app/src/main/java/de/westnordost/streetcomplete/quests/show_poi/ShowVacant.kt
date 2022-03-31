package de.westnordost.streetcomplete.quests.show_poi

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.*
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.osm.KEYS_THAT_SHOULD_BE_REMOVED_WHEN_SHOP_IS_REPLACED
import de.westnordost.streetcomplete.osm.removeCheckDates
import de.westnordost.streetcomplete.osm.updateCheckDate
import de.westnordost.streetcomplete.quests.shop_type.IsShopVacant
import de.westnordost.streetcomplete.quests.shop_type.ShopType
import de.westnordost.streetcomplete.quests.shop_type.ShopTypeAnswer
import de.westnordost.streetcomplete.quests.shop_type.ShopTypeForm

class ShowVacant : OsmFilterQuestType<ShopTypeAnswer>() {
    override val elementFilter = """
        nodes, ways, relations with
        shop = vacant
        or disused:shop
        or disused:amenity
        or disused:office
    """
    override val changesetComment = "Check if vacant shop is still vacant"
    override val wikiLink = "Key:disused:"
    override val icon = R.drawable.ic_quest_wheelchair_shop
    override val dotColor = "grey"
    override val defaultDisabledMessage = R.string.default_disabled_msg_poi_vacant

    override fun getTitle(tags: Map<String, String>) =
        R.string.quest_poi_vacant_title

    override fun createForm() = ShopTypeForm()

    override fun getTitleArgs(tags: Map<String, String>): Array<String> {
        return arrayOf(tags.entries.toString())
    }

    override fun applyAnswerTo(answer: ShopTypeAnswer, tags: Tags, timestampEdited: Long) {
        when (answer) {
            is IsShopVacant -> {
                tags.updateCheckDate()
            }
            is ShopType -> {
                tags.removeCheckDates()

                for (key in tags.keys) {
                    if (KEYS_THAT_SHOULD_BE_REMOVED_WHEN_SHOP_IS_REPLACED.any { it.matches(key) }) {
                        tags.remove(key)
                    }
                }

                for ((key, value) in answer.tags) {
                    tags[key] = value
                }
            }
        }
    }
}
