package de.westnordost.streetcomplete.quests.show_poi

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.getLabelSources
import de.westnordost.streetcomplete.quests.LabelOrElementSelectionDialog
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.LocalElement
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

class ShowRecycling : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways, relations with
          amenity ~ recycling|waste_basket|waste_disposal|waste_transfer_station|sanitary_dump_station
    """
    override val changesetComment = "Adjust recycling related elements"
    override val wikiLink = "Key:amenity=recycling"
    override val icon = Res.drawable.ic_quest_poi_recycling
    override val title = Res.string.quest_poi_recycling_title
    override val dotColor = "green"
    override val defaultDisabledMessage = Res.string.default_disabled_msg_poi_recycling
    override val dotLabelSources = getLabelSources( "", this, prefs)

    @Composable
    override fun Form(on: (QuestAction<Boolean>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        val originalSubtitle = LocalElement.current?.let { element ->
            nameAndLocationLabel(element, koinInject())
        }
        val recycling = element.tags.mapNotNull {
            if (it.value == "yes" && it.key.startsWith("recycling:"))
                it.key.substringAfter("recycling:")
            else null
        }.sorted().joinToString(", ")
        val subtitle = if (recycling.isNotEmpty())
            (originalSubtitle ?: AnnotatedString("")) + AnnotatedString(" $recycling")
            else originalSubtitle
        QuestForm(
            on,
            answers = listOfNotNull(if (element.tags["amenity"] == "waste_basket") AnswerItem(
                stringResource(Res.string.quest_recycling_excrement_bag_dispenser)) {
                on(Answer(true))
            } else null),
            subtitle = subtitle
        )
    }

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter(filter)

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (answer) {
            tags["amenity"] = "vending_machine"
            tags["vending"] = "excrement_bags"
            tags["bin"] = "yes"
        }
    }

    @Composable
    override fun QuestSettings(onDismissRequest: () -> Unit) {
        LabelOrElementSelectionDialog(this, prefs, onDismissRequest)
    }
}
