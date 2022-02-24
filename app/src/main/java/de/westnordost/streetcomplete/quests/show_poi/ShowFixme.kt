package de.westnordost.streetcomplete.quests.show_poi

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags

class ShowFixme : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways, relations with
          (fixme or FIXME)
          and fixme !~ "continue|continue?|yes|Baum oder Strauch|Use for reference, adjust surroundings using the description, but please do NOT move."
          and FIXME !~ continue|continue?|yes
    """
    override val changesetComment = "Remove fixme"
    override val wikiLink = "Key:fixme"
    override val icon = R.drawable.ic_quest_create_note
    override val dotColor = "red"
    override val defaultDisabledMessage = R.string.default_disabled_msg_poi_fixme

    override fun getTitle(tags: Map<String, String>) = R.string.quest_fixme_title

    override fun createForm() = ShowFixmeAnswerForm()

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val name = tags.entries.mapNotNull {
            when (it.key) {
                "fixme", "FIXME" -> null
                else -> it
            }
        }
        val fixme = tags["fixme"] ?: tags["FIXME"]
        return arrayOf(fixme.toString(),name.toString())
    }

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        if (!answer) {
            tags.remove("fixme")
            tags.remove("FIXME")
        }
    }
}