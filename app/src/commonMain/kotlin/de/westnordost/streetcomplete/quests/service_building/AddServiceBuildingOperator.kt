package de.westnordost.streetcomplete.quests.service_building

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.Action
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.preferences.addLastPicked
import de.westnordost.streetcomplete.data.preferences.getLastPicked
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.NameWithSuggestionsQuestForm
import de.westnordost.streetcomplete.util.takeFavorites
import org.jetbrains.compose.resources.stringResource
import kotlin.String
import kotlin.collections.plus

class AddServiceBuildingOperator : OsmFilterQuestType<ServiceBuildingOperatorAnswer>() {

    override val elementFilter = """
        ways, relations with
          building ~ service|transformer_tower
          and !operator
          and !name
          and !brand
          and disused != yes and abandoned != yes and !construction
    """
    override val changesetComment = "Add service building operator"
    override val wikiLink = "Tag:building=service"
    override val icon = Res.drawable.ic_quest_service_building
    override val title = Res.string.quest_service_building_operator_title
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    @Composable
    override fun Form(on: (QuestAction<ServiceBuildingOperatorAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        val lastPickedAnswers = prefs.getLastPicked<String>(this::class.simpleName!!).takeFavorites(50, 50, 1)
        NameWithSuggestionsQuestForm(
            { on(when (it) {
                is Answer<String> -> Answer(ServiceBuildingOperator(it.value))
                is Action -> it
            }) },
            suggestions = (lastPickedAnswers + OPERATORS).distinct(),
            otherAnswers = { listOf(AnswerItem(stringResource(Res.string.quest_disused)) { on(Answer(DisusedServiceBuilding)) }) },
            showSuggestionsOnStart = true
        )
    }

    override fun applyAnswerTo(answer: ServiceBuildingOperatorAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is ServiceBuildingOperator -> {
                prefs.addLastPicked(this::class.simpleName!!, answer.name)
                tags["operator"] = answer.name
            }
            is DisusedServiceBuilding -> {
                tags["disused"] = "yes"
                tags.keys.toList().filter { it.matches(Regex("^(power|service|man_made|substation|pipeline|utility|railway)$")) }
                    .forEach {
                        tags["disused:" + it] = tags[it] ?: "yes"
                        tags.remove(it)
                    }
            }
        }
    }
}

private val OPERATORS = listOf(
    "Wiener Netze", "Wien Energie", "Wienstrom", "EVN", "Netz Niederösterreich GmbH", "Netz OÖ",
    "Salzburg AG", "KNG-Kärnten Netz GmbH", "Energie Steiermark",
    "ÖBB", "GKB", "Wiener Linien",
    "e.on", "DPMB",
)
