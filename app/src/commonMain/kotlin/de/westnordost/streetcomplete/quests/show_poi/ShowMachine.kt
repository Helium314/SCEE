package de.westnordost.streetcomplete.quests.show_poi

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.getLabelSources
import de.westnordost.streetcomplete.quests.LabelOrElementSelectionDialog
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.LocalElement
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import kotlin.text.equals

class ShowMachine : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways with
          amenity ~ vending_machine|atm|telephone|charging_station|device_charging_station|photo_booth
          or atm = yes and (amenity or shop)
    """
    override val changesetComment = "Adjust vending machine or similar"
    override val wikiLink = "Tag:amenity=vending_machine"
    override val icon = Res.drawable.ic_quest_poi_machine
    override val title = Res.string.quest_poi_machine_title
    override val dotColor = "blue"
    override val defaultDisabledMessage = Res.string.default_disabled_msg_poi_machine
    override val dotLabelSources = getLabelSources("vending", this, prefs)

    @Composable
    override fun Form(on: (QuestAction<Boolean>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        val titleRes = if (!element.tags["atm"].isNullOrEmpty() && element.tags["atm"] != "no")
                Res.string.quest_poi_has_atm_title
            else if (element.tags["amenity"].equals("vending_machine"))
                Res.string.quest_poi_vending_title
            else
                Res.string.quest_poi_machine_title
        val originalSubtitle = LocalElement.current?.let { element ->
            nameAndLocationLabel(element, koinInject())
        }
        QuestForm(
            on,
            answers = listOf(),
            title = stringResource(titleRes),
            subtitle = if (element.tags["amenity"] == "vending_machine" && element.tags.contains("vending"))
                    (originalSubtitle ?: AnnotatedString("")) + AnnotatedString(" ${element.tags["vending"]}")
                else originalSubtitle
        )
    }

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter(filter)

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {}

    @Composable
    override fun QuestSettings(onDismissRequest: () -> Unit) {
        LabelOrElementSelectionDialog(this, prefs, onDismissRequest)
    }
}
