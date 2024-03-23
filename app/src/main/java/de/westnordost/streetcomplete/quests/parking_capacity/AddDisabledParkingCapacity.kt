package de.westnordost.streetcomplete.quests.parking_capacity

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.Tags

class AddDisabledParkingCapacity : OsmFilterQuestType<Int>() {

    override val elementFilter = """
        nodes, ways with
         amenity = parking
         and access !~ private|no
         and !capacity:disabled
    """

    override val changesetComment = "Specify disabled parking capacities"
    override val wikiLink = "Key:capacity:disabled"
    override val icon = R.drawable.ic_quest_parking_capacity_disabled
    override val achievements = listOf(WHEELCHAIR)
    // set the default disabled msg
    override val defaultDisabledMessage = R.string.quest_parking_capacity_disabled_default_disabled_msg

    override fun getTitle(tags: Map<String, String>) = R.string.quest_parking_capacity_disabled_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways with amenity = parking")

    override fun createForm() = AddDisabledParkingCapacityForm.create(showClarificationText = false)

    override fun applyAnswerTo(answer: Int, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (answer == -1) {
            tags["capacity:disabled"] = "yes"
        }
        else if (answer == 0) {
            tags["capacity:disabled"] = "no"
        }
        else {
            tags["capacity:disabled"] = answer.toString()
        }
    }
}
