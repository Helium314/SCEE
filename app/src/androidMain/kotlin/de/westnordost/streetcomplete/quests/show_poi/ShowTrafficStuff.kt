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
import de.westnordost.streetcomplete.resources.default_disabled_msg_poi_traffic
import de.westnordost.streetcomplete.resources.quest_poi_traffic_title

class ShowTrafficStuff : OsmFilterQuestType<Boolean>(), AndroidQuest {
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
    override val icon = R.drawable.ic_quest_poi_traffic
    override val title = Res.string.quest_poi_traffic_title
    override val dotColor = "deepskyblue"
    override val defaultDisabledMessage = Res.string.default_disabled_msg_poi_traffic
    override val dotLabelSources = getLabelSources( "", this, prefs)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter(filter)

    override fun createForm() = ShowTrafficStuffAnswerForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (answer)
            tags["traffic_calming"] = "table"
    }

    @Composable
    override fun QuestSettings(onDismissRequest: () -> Unit) {
        LabelOrElementSelectionDialog(this, prefs, onDismissRequest)
    }
}
