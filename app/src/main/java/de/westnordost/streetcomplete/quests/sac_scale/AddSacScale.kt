package de.westnordost.streetcomplete.quests.sac_scale

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.via_ferrata_scale.ViaFerrataScale

class AddSacScale : OsmFilterQuestType<SacScale>() {

    override val elementFilter = """
        ways with
          highway ~ path
          and !sac_scale
          and (!lit or lit = no)
    """
    override val changesetComment = "Specify SAC Scale"
    override val wikiLink = "Key:sac_scale"
    override val icon = R.drawable.ic_quest_trail_visibility
    override val defaultDisabledMessage = R.string.default_disabled_msg_sacScale

    override fun getTitle(tags: Map<String, String>) = R.string.quest_sacScale_title

    override fun getHighlightedElements(
        element: Element,
        getMapData: () -> MapDataWithGeometry
    ) = getMapData().filter("ways with highway and sac_scale")

    override fun createForm() = AddSacScaleForm()

    override fun applyAnswerTo(
        answer: SacScale,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long
    ) {
        tags["sac_scale"] = answer.osmValue
    }
}
