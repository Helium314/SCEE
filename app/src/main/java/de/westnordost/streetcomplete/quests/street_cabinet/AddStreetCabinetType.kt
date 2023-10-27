package de.westnordost.streetcomplete.quests.street_cabinet

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags

class AddStreetCabinetType : OsmFilterQuestType<StreetCabinetType>() {

    override val elementFilter = """
        ways, relations with
          man_made = street_cabinet
          and !street_cabinet
          and !utility
    """
    override val changesetComment = "Add street cabinet type"
    override val wikiLink = "Tag:man_made=street_cabinet"
    override val icon = R.drawable.ic_quest_service_building
    override val defaultDisabledMessage: Int = R.string.default_disabled_msg_ee

    override fun getTitle(tags: Map<String, String>) = R.string.quest_street_cabinet_type_title

    override fun getTitleArgs(tags: Map<String, String>): Array<String> {
        val operator = tags["operator"]?.let { " ($it)" } ?: ""
        return arrayOf(operator)
    }

    override fun createForm() = AddStreetCabinetTypeForm()

    override fun applyAnswerTo(
        answer: StreetCabinetType,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long
    ) {
        answer.tags.forEach { tags[it.first] = it.second }
        /*
        if (answer == StreetCabinetType.VENTILATION_SHAFT)
            tags.remove("building") // see https://wiki.openstreetmap.org/wiki/Tag:man_made%3Dventilation_shaft
    }
    */
    }
}
