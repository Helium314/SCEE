package de.westnordost.streetcomplete.quests.brewery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.Action
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.places.isPlace
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.MultiValueQuestForm
import de.westnordost.streetcomplete.util.math.enlargedBy
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import kotlin.sequences.forEach

class AddBrewery : OsmFilterQuestType<BreweryAnswer>() {

    override val elementFilter = """
        nodes, ways with
          amenity ~ bar|biergarten|pub|restaurant|nightclub
          and drink:beer != no
          and (
            brewery ~ yes|no
            or !brewery
            or brewery older today -6 years
          )
    """
    override val changesetComment = "Add brewery"
    override val wikiLink = "Key:brewery"
    override val icon = Res.drawable.ic_quest_brewery
    override val title = Res.string.quest_brewery_title
    override val defaultDisabledMessage = Res.string.default_disabled_msg_go_inside

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.asSequence().filter { it.isPlace() }

    @Composable
    override fun Form(on: (QuestAction<BreweryAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        if (suggestions.isEmpty())
            suggestions.addAll(LocalContext.current.assets.open("brewery/brewerySuggestions.txt").bufferedReader().readLines())
        val mapDataSource: MapDataWithEditsSource = koinInject()
        fun getNearbySuggestions(): Collection<String> {
            val data = mapDataSource.getMapDataWithGeometry(geometry.bounds.enlargedBy(100.0))
            val suggestions = hashSetOf<String>()
            data.filter("nodes, ways with brewery").forEach {
                it.tags["brewery"]?.let { suggestions.addAll(it.split(";")) }
            }
            suggestions.remove("yes")
            suggestions.remove("various")
            suggestions.remove("no")
            return suggestions
        }
        val nearbySuggestion = remember { getNearbySuggestions() }
        MultiValueQuestForm(
            { on(when (it) {
                is Answer<String> -> Answer(BreweryStringAnswer(it.value))
                is Action -> it
            }) },
            Res.string.quest_brewery_add_more,
            simpleSuggestions = suggestions,
            prioritySuggestions = { nearbySuggestion },
            otherAnswers = { listOf(
                AnswerItem(stringResource(Res.string.quest_brewery_is_not_available)) { on(Answer(NoBeerAnswer))},
                AnswerItem(stringResource(Res.string.quest_brewery_is_various)) { on(Answer(ManyBeersAnswer))},
            ) }
        )
    }

    override fun applyAnswerTo(answer: BreweryAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is NoBeerAnswer -> {
                tags["drink:beer"] = "no"
                if (tags["brewery"] != "no") // don't remove brewery=no
                    tags.remove("brewery")
            }
            is ManyBeersAnswer -> tags["brewery"] = "various"
            is BreweryStringAnswer -> tags["brewery"] = answer.brewery
        }
    }
}

private val suggestions = mutableListOf<String>()
