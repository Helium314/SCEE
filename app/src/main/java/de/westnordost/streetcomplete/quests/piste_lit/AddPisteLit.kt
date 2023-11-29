package de.westnordost.streetcomplete.quests.piste_lit

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.isWinter
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddPisteLit : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        ways, relations with
          piste:type ~ downhill|nordic|sled|ski_jump|ice_skate
        and (
          !piste:lit
          or piste:lit = no and lit older today -8 years
          or piste:lit older today -16 years
        )
    """
    override val changesetComment = "Specify whether pistes are lit"
    override val wikiLink = "Key:piste:lit"
    override val icon = R.drawable.ic_quest_piste_lit
    override val defaultDisabledMessage = R.string.default_disabled_msg_ee

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        return if (isWinter(mapData.nodes.firstOrNull()?.position)) mapData.filter(filter).asIterable()
        else emptyList()
    }

    override fun isApplicableTo(element: Element) = if (filter.matches(element)) null else false

    override fun getTitle(tags: Map<String, String>) = R.string.quest_piste_lit_title

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("piste:lit", answer.toYesNo())
    }
}