package de.westnordost.streetcomplete.quests.parking_capacity

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.CountInputQuestForm
import org.jetbrains.compose.resources.painterResource

class AddDisabledParkingCapacity : OsmFilterQuestType<Int>() {

    override val elementFilter = """
        nodes, ways with
         amenity = parking
         and access !~ private|no
         and !capacity:disabled
    """

    override val changesetComment = "Specify disabled parking capacities"
    override val wikiLink = "Key:capacity:disabled"
    override val icon = Res.drawable.ic_quest_parking_capacity_disabled
    override val title = Res.string.quest_parking_capacity_disabled_title
    override val achievements = listOf(WHEELCHAIR)
    override val defaultDisabledMessage = Res.string.quest_parking_capacity_disabled_default_disabled_msg

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with amenity = parking")

    @Composable
    override fun Form(on: (QuestAction<Int>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        CountInputQuestForm(
            on = on,
            icon = painterResource(Res.drawable.wheelchair_sign),
        )
    }

    override fun applyAnswerTo(answer: Int, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
            tags["capacity:disabled"] = when (answer) {
                 0 -> "no"
                -1 -> "yes"
                else -> answer.toString()
            }
    }
}
