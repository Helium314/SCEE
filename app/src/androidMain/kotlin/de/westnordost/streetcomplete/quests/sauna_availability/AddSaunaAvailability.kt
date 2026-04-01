package de.westnordost.streetcomplete.quests.sauna_availability

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_ee
import de.westnordost.streetcomplete.resources.quest_saunaAvailability_title

class AddSaunaAvailability : OsmFilterQuestType<Boolean>(), AndroidQuest {

    override val elementFilter = """
        nodes, ways with
        (
          leisure ~ fitness_centre
          or leisure = sports_hall and sport = swimming
          or tourism ~ camp_site|hotel
        )
        and !sauna
    """
    override val changesetComment = "Survey sauna availabilities"
    override val wikiLink = "Key:sauna"
    override val title = Res.string.quest_saunaAvailability_title
    override val icon = R.drawable.ic_quest_sauna
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["sauna"] = answer.toYesNo()
    }
}
