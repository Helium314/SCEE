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
import de.westnordost.streetcomplete.resources.default_disabled_msg_poi_machine
import de.westnordost.streetcomplete.resources.quest_poi_has_atm_title
import de.westnordost.streetcomplete.resources.quest_poi_machine_title
import de.westnordost.streetcomplete.resources.quest_poi_vending_title

class ShowMachine : OsmFilterQuestType<Boolean>(), AndroidQuest {
    override val elementFilter = """
        nodes, ways with
          amenity ~ vending_machine|atm|telephone|charging_station|device_charging_station|photo_booth
          or atm = yes and (amenity or shop)
    """
    override val changesetComment = "Adjust vending machine or similar"
    override val wikiLink = "Tag:amenity=vending_machine"
    override val icon = R.drawable.ic_quest_poi_machine
    override val title = Res.string.quest_poi_machine_title
    override val dotColor = "blue"
    override val defaultDisabledMessage = Res.string.default_disabled_msg_poi_machine
    override val dotLabelSources = getLabelSources("vending", this, prefs)

    override fun getTitle(tags: Map<String, String>) =
        if (!tags["atm"].isNullOrEmpty() && tags["atm"] != "no")
            Res.string.quest_poi_has_atm_title
        else if (tags["amenity"].equals("vending_machine"))
            Res.string.quest_poi_vending_title
        else
            Res.string.quest_poi_machine_title

    override fun createForm() = ShowMachineAnswerForm()

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter(filter)

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {}

    @Composable
    override fun QuestSettings(onDismissRequest: () -> Unit) {
        LabelOrElementSelectionDialog(this, prefs, onDismissRequest)
    }
}
