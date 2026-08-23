package de.westnordost.streetcomplete.quests.parking_orientation

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition
import de.westnordost.streetcomplete.osm.street_parking.StreetParking
import de.westnordost.streetcomplete.osm.street_parking.painter
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.stringResource

class AddParkingOrientation : OsmFilterQuestType<ParkingOrientation>() {

    override val elementFilter = """
        nodes, ways, relations with
          amenity = parking
          and parking ~ "lane|street_side|on_kerb|half_on_kerb"
          and !orientation
    """
    override val changesetComment = "Specify parking orientation"
    override val wikiLink = "Key:orientation"
    override val icon = Res.drawable.ic_quest_parking_orientation
    override val title = Res.string.quest_parking_orientation_title
    override val achievements = listOf(CAR)


    @Composable
    override fun Form(on: (QuestAction<ParkingOrientation>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        val position = when (element.tags["parking"]) {
            "street_side" -> ParkingPosition.STREET_SIDE
            "on_kerb" -> ParkingPosition.OFF_STREET
            "half_on_kerb" -> ParkingPosition.HALF_ON_STREET
            else -> ParkingPosition.ON_STREET
        }

        ItemSelectQuestForm(
            on = on,
            items = ParkingOrientation.entries,
            itemContent = {
                ImageWithLabel(
                    StreetParking.PositionAndOrientation(it, position).painter(isUpsideDown = false, isRightSide = false),
                    stringResource(it.title)
                )
            },
        )
    }

    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    override fun applyAnswerTo(answer: ParkingOrientation, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["orientation"] = answer.osmValue
    }
}
