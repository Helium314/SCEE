package de.westnordost.streetcomplete.quests.show_poi

import androidx.compose.runtime.Composable
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
import de.westnordost.streetcomplete.ui.common.quest.QuestForm

class ShowSeating : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways, relations with
        amenity ~ bench|lounger|table
        or leisure ~ picnic_table|bleachers
        or tourism = picnic_site
    """
    override val changesetComment = "Adjust benches and similar"
    override val wikiLink = "Tag:amenity=bench"
    override val icon = Res.drawable.ic_quest_poi_seating
    override val title = Res.string.quest_poi_seating_title
    override val dotColor = "chocolate"
    override val defaultDisabledMessage = Res.string.default_disabled_msg_poi_bench
    override val dotLabelSources = getLabelSources( "", this, prefs)

    @Composable
    override fun Form(on: (QuestAction<Boolean>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        QuestForm(on, answers = listOf())
    }

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter(filter)

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {}

    @Composable
    override fun QuestSettings(onDismissRequest: () -> Unit) {
        LabelOrElementSelectionDialog(this, prefs, onDismissRequest)
    }
}
