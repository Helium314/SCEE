package de.westnordost.streetcomplete.quests.contact

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.places.isPlace
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_ee
import de.westnordost.streetcomplete.resources.ic_quest_website
import de.westnordost.streetcomplete.resources.quest_contact_website
import de.westnordost.streetcomplete.ui.common.quest.TextInputForm

class AddContactWebsite : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes, ways, relations with
        (
         tourism = information and information = office
         or """.trimIndent() +
        PLACES_FOR_CONTACT_QUESTS +
        "\n) and !website and !contact:website and !contact:facebook and !contact:instagram and !brand and (name or operator)"

    override val changesetComment = "Add website"
    override val wikiLink = "Key:website"
    override val icon = Res.drawable.ic_quest_website
    override val title = Res.string.quest_contact_website
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.asSequence().filter { it.isPlace() }

    @Composable
    override fun Form(on: (QuestAction<String>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        TextInputForm(on, keyboardType = KeyboardType.Uri, initialValue = prefill, isOk = { it != prefill && it.contains(".") })
    }

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        prefill = if (answer.contains("//"))
            answer.substringBefore("//") + "//"
        else
            ""
        tags["website"] = answer
    }
}

private var prefill = "http://"
