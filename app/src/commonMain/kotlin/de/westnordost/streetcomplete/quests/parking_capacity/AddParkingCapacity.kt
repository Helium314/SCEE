package de.westnordost.streetcomplete.quests.parking_capacity

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.CountInputQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddParkingCapacity : OsmFilterQuestType<Int>() {

    override val elementFilter = """
        nodes, ways with
         amenity = parking
         and parking = surface
         and access !~ private|no
         and !capacity
    """

    override val changesetComment = "Specify parking capacities"
    override val wikiLink = "Tag:amenity=parking"
    override val icon = Res.drawable.ic_quest_parking_capacity
    override val title = Res.string.quest_parking_capacity_title
    override val hint = Res.string.quest_parking_capacity_hint
    override val achievements = listOf(CAR)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with amenity = parking")

    @Composable
    override fun Form(on: (QuestAction<Int>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        CountInputQuestForm(
            on = on,
            icon = painterResource(Res.drawable.ic_car),
            otherAnswers = {
                if (element.tags["capacity:disabled"] != "yes")
                    listOf(AnswerItem(stringResource(Res.string.quest_parking_capacity_disabled_answer_yes)) {
                        on(Answer(-1))
                    })
                else emptyList()
            }
        )
    }

    override fun applyAnswerTo(answer: Int, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("capacity", answer.toString())
    }
}
