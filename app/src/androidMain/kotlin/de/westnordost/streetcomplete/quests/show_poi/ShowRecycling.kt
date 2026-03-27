package de.westnordost.streetcomplete.quests.show_poi

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.getLabelSources
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.quests.LabelOrElementSelectionDialog
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_poi_recycling
import de.westnordost.streetcomplete.resources.quest_poi_recycling_title

class ShowRecycling : OsmFilterQuestType<Boolean>(), AndroidQuest {
    override val elementFilter = """
        nodes, ways, relations with
          amenity ~ recycling|waste_basket|waste_disposal|waste_transfer_station|sanitary_dump_station
    """
    override val changesetComment = "Adjust recycling related elements"
    override val wikiLink = "Key:amenity=recycling"
    override val icon = R.drawable.ic_quest_poi_recycling
    override val title = Res.string.quest_poi_recycling_title
    override val dotColor = "green"
    override val defaultDisabledMessage = Res.string.default_disabled_msg_poi_recycling
    override val dotLabelSources = getLabelSources( "", this, prefs)

    override fun createForm() = ShowRecyclingAnswerForm()

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
