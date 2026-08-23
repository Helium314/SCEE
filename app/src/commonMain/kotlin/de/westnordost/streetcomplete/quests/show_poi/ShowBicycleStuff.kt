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
import de.westnordost.streetcomplete.quests.LabelOrElementSelectionDialog
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.QuestForm

class ShowBicycleStuff : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways, relations with
          amenity ~ bicycle_parking|bicycle_rental|bicycle_repair_station|compressed_air
    """
    override val changesetComment = "Adjust bicycle related elements"
    override val wikiLink = "Tag:amenity=bicycle_parking"
    override val icon = Res.drawable.ic_quest_poi_bicycle
    override val title = Res.string.quest_poi_cycling_title
    override val dotColor = "mediumorchid"
    override val defaultDisabledMessage = Res.string.default_disabled_msg_poi_bike

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
