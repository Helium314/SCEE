package de.westnordost.streetcomplete.quests.mtb_scale

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags

class AddMtbScale : OsmFilterQuestType<MtbScale>() {

    override val elementFilter = """
        ways with
          highway ~ path|track
          and !mtb:scale
          and bicycle !~ no|private
          and (!lit or lit = no)
          and surface ~ "grass|sand|dirt|soil|fine_gravel|compacted|wood|gravel|pebblestone|rock|ground|earth|mud|woodchips|snow|ice|salt|stone"
    """
    override val changesetComment = "Specify MTB Scale"
    override val wikiLink = "Key:mtb:scale"
    override val icon = R.drawable.ic_quest_mtb_scale
    override val defaultDisabledMessage = R.string.default_disabled_msg_mtbScale

    override fun getTitle(tags: Map<String, String>) = R.string.quest_mtbScale_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("ways with highway and mtb:scale")

    override fun createForm() = AddMtbScaleForm()

    override fun applyAnswerTo(answer: MtbScale, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["mtb:scale"] = answer.osmValue
    }
}
