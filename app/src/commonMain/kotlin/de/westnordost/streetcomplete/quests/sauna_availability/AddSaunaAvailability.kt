package de.westnordost.streetcomplete.quests.sauna_availability

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.util.ktx.toYesNo
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.YesNoQuestForm

class AddSaunaAvailability : OsmFilterQuestType<Boolean>() {

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
    override val icon = Res.drawable.ic_quest_sauna
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    @Composable
    override fun Form(on: (QuestAction<Boolean>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        YesNoQuestForm(on)
    }

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["sauna"] = answer.toYesNo()
    }
}
