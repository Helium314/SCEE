package de.westnordost.streetcomplete.quests.street_cabinet

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_ee
import de.westnordost.streetcomplete.resources.quest_street_cabinet_type_title

class AddStreetCabinetType : OsmFilterQuestType<StreetCabinetType>(), AndroidQuest {

    override val elementFilter = """
        nodes, ways with
          man_made = street_cabinet
          and !street_cabinet
          and !utility
    """
    override val changesetComment = "Add street cabinet type"
    override val wikiLink = "Tag:man_made=street_cabinet"
    override val icon = R.drawable.ic_quest_street_cabinet
    override val title = Res.string.quest_street_cabinet_type_title
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("""
            nodes, ways with
             (
                 man_made = street_cabinet
                 or building ~ service|transformer_tower
             )
        """)

    override fun createForm() = AddStreetCabinetTypeForm()

    override fun applyAnswerTo(answer: StreetCabinetType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags[answer.osmKey] = answer.osmValue
    }
}
