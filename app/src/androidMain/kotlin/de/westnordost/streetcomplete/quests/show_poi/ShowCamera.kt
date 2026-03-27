package de.westnordost.streetcomplete.quests.show_poi

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.NoAnswerFragment
import de.westnordost.streetcomplete.quests.getLabelSources
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.quests.LabelOrElementSelectionDialog
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_poi_camera
import de.westnordost.streetcomplete.resources.quest_poi_camera_title

class ShowCamera : OsmFilterQuestType<Boolean>(), AndroidQuest {
    override val elementFilter = """
        nodes, ways, relations with
          man_made = surveillance
    """
    override val changesetComment = "Adjust surveillance cameras"
    override val wikiLink = "Tag:surveillance:type"
    override val icon = R.drawable.ic_quest_poi_camera
    override val title = Res.string.quest_poi_camera_title
    override val dotColor = "mediumvioletred"
    override val defaultDisabledMessage = Res.string.default_disabled_msg_poi_camera
    override val dotLabelSources = getLabelSources("", this, prefs)

    override fun createForm() = NoAnswerFragment()

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter(filter)

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {}

    @Composable
    override fun QuestSettings(onDismissRequest: () -> Unit) {
        LabelOrElementSelectionDialog(this, prefs, onDismissRequest)
    }
}
