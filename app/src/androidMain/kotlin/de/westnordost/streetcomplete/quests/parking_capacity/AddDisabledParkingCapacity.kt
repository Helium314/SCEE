package de.westnordost.streetcomplete.quests.parking_capacity

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_parking_capacity_disabled_default_disabled_msg
import de.westnordost.streetcomplete.resources.quest_parking_capacity_disabled_title

class AddDisabledParkingCapacity : OsmFilterQuestType<Int>(), AndroidQuest {

    override val elementFilter = """
        nodes, ways with
         amenity = parking
         and access !~ private|no
         and !capacity:disabled
    """

    override val changesetComment = "Specify disabled parking capacities"
    override val wikiLink = "Key:capacity:disabled"
    override val icon = R.drawable.ic_quest_parking_capacity_disabled
    override val title = Res.string.quest_parking_capacity_disabled_title
    override val achievements = listOf(WHEELCHAIR)
    override val defaultDisabledMessage = Res.string.quest_parking_capacity_disabled_default_disabled_msg

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with amenity = parking")

    override fun createForm() = AddDisabledParkingCapacityForm()

    override fun applyAnswerTo(answer: Int, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
            tags["capacity:disabled"] = when (answer) {
                 0 -> "no"
                -1 -> "yes"
                else -> answer.toString()
            }
    }
}
