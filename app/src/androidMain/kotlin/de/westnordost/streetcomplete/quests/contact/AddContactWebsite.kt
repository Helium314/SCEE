package de.westnordost.streetcomplete.quests.contact

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isPlace
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_ee
import de.westnordost.streetcomplete.resources.quest_contact_website

class AddContactWebsite : OsmFilterQuestType<String>(), AndroidQuest {

    override val elementFilter = """
        nodes, ways, relations with
        (
         tourism = information and information = office
         or """.trimIndent() +
        PLACES_FOR_CONTACT_QUESTS +
        "\n) and !website and !contact:website and !contact:facebook and !contact:instagram and !brand and (name or operator)"

    override val changesetComment = "Add website"
    override val wikiLink = "Key:website"
    override val icon = R.drawable.ic_quest_website
    override val title = Res.string.quest_contact_website
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.asSequence().filter { it.isPlace() }

    override fun createForm() = AddContactWebsiteForm()

    override val isReplacePlaceEnabled: Boolean = true

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["website"] = answer
    }
}
