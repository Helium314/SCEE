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

class ShowTrafficStuff : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways with
         barrier and barrier !~ wall|fence|retaining_wall|hedge
         or traffic_calming
         or traffic_sign
         or crossing
         or entrance
         or public_transport
         or highway ~ crossing|stop|give_way|elevator|traffic_signals|turning_circle
         or amenity ~ taxi|parking|parking_entrance|motorcycle_parking
         """

    override val changesetComment = "Adjust traffic related elements"
    override val wikiLink = "Key:traffic_calming"
    override val icon = Res.drawable.ic_quest_poi_traffic
    override val title = Res.string.quest_poi_traffic_title
    override val dotColor = "deepskyblue"
    override val defaultDisabledMessage = Res.string.default_disabled_msg_poi_traffic
    override val dotLabelSources = getLabelSources( "", this, prefs)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter(filter)

    @Composable
    override fun Form(on: (QuestAction<Boolean>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        val originalSubtitle = LocalElement.current?.let { element ->
            nameAndLocationLabel(element, koinInject())
        }
        val subtitle = if ((!element.tags["crossing"].isNullOrBlank() && !element.tags["traffic_calming"].isNullOrBlank())
            || element.tags["type"] == "restriction"
            || element.tags["highway"] == "elevator")
                (originalSubtitle ?: AnnotatedString("")) + AnnotatedString(" ${element.tags.entries}")
            else originalSubtitle
        QuestForm(
            on,
            answers = listOfNotNull(if (element.tags["traffic_calming"] == null && element.tags["crossing"] != null)
                AnswerItem(stringResource(Res.string.quest_traffic_stuff_raised)) { on(Answer(true)) }
            else null),
            subtitle = subtitle
        )
    }

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (answer)
            tags["traffic_calming"] = "table"
    }

    @Composable
    override fun QuestSettings(onDismissRequest: () -> Unit) {
        LabelOrElementSelectionDialog(this, prefs, onDismissRequest)
    }
}
