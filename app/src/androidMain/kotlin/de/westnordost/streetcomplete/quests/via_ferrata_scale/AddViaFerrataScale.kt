package de.westnordost.streetcomplete.quests.via_ferrata_scale

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_viaFerrataScale
import de.westnordost.streetcomplete.resources.quest_viaFerrataScale_title

class AddViaFerrataScale : OsmFilterQuestType<ViaFerrataScale>(), AndroidQuest {

    override val elementFilter = """
        ways with
          highway = via_ferrata
          and !via_ferrata_scale
    """
    override val changesetComment = "Specify Via Ferrata Grade Scale"
    override val wikiLink = "Key:via_ferrata_scale"
    override val icon = R.drawable.ic_quest_via_ferrata_scale
    override val title = Res.string.quest_viaFerrataScale_title
    override val defaultDisabledMessage = Res.string.default_disabled_msg_viaFerrataScale

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("ways with highway = via_ferrata")

    override fun createForm() = AddViaFerrataScaleForm()

    override fun applyAnswerTo(answer: ViaFerrataScale, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["via_ferrata_scale"] = answer.osmValue
    }
}
