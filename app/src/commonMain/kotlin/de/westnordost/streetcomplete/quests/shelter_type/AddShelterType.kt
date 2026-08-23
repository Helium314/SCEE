package de.westnordost.streetcomplete.quests.shelter_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddShelterType : OsmFilterQuestType<ShelterType>() {

    override val elementFilter = """
        nodes, ways with
          amenity = shelter
          and !shelter_type
    """
    override val changesetComment = "Specify shelter types"
    override val wikiLink = "Key:shelter_type"
    override val icon = Res.drawable.ic_quest_shelter_type
    override val title = Res.string.quest_shelter_type_title
    override val achievements = listOf(EditTypeAchievement.OUTDOORS)
    override val defaultDisabledMessage = Res.string.quest_shelter_type_disabled_msg

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with amenity = shelter")

    @Composable
    override fun Form(on: (QuestAction<ShelterType>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            on = on,
            items = ShelterType.entries.filterNot { it == ShelterType.WEATHER_SHELTER },
            itemContent = { ImageWithLabel(painterResource(it.icon!!), stringResource(it.title!!)) },
            otherAnswers = { listOf(AnswerItem(stringResource(Res.string.quest_shelter_type_is_weather_shelter)) { on(Answer(ShelterType.WEATHER_SHELTER)) }) }
        )
    }

    override fun applyAnswerTo(answer: ShelterType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["shelter_type"] = answer.osmValue
    }
}
