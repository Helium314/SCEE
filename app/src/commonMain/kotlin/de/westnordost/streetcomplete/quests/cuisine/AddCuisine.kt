package de.westnordost.streetcomplete.quests.cuisine

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.places.isPlace
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.MultiValueQuestForm

class AddCuisine : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes, ways with
        (
          amenity ~ restaurant|fast_food
          or (amenity = pub and food = yes)
        )
        and !cuisine
    """
    override val changesetComment = "Add cuisine"
    override val wikiLink = "Key:cuisine"
    override val icon = Res.drawable.quest_restaurant
    override val title = Res.string.quest_cuisine_title
    override val defaultDisabledMessage = Res.string.default_disabled_msg_go_inside

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.asSequence().filter { it.isPlace() }

    @Composable
    override fun Form(on: (QuestAction<String>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        if (suggestions.isEmpty())
            suggestions.addAll(LocalContext.current.assets.open("cuisine/cuisineSuggestions.txt.txt").bufferedReader().readLines())
        MultiValueQuestForm(
            on,
            Res.string.quest_cuisine_add_more,
            simpleSuggestions = suggestions,
        )
    }

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["cuisine"] = answer
    }
}

private val suggestions = mutableListOf<String>()
