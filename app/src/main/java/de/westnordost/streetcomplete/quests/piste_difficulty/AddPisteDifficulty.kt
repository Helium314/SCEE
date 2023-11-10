package de.westnordost.streetcomplete.quests.piste_difficulty

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.osm.Tags

class AddPisteDifficulty : OsmFilterQuestType<List<PisteDifficulty>>() {

    override val elementFilter = """
        ways, relations with
          piste:type ~ downhill|nordic|skitour
          and !piste:difficulty
    """
    override val changesetComment = "Add piste difficulty"
    override val wikiLink = "Key:piste:difficulty"
    override val icon = R.drawable.ic_quest_piste_difficulty
    override val defaultDisabledMessage: Int = R.string.default_disabled_msg_ee
    override val enabledInCountries = NoCountriesExcept("AT", "CZ", "FI", "FR", "DE", "IT", "LI", "NO", "SE", "SI")

    override fun getTitle(tags: Map<String, String>) = R.string.quest_piste_difficulty_title

    override fun getTitleArgs(tags: Map<String, String>): Array<String> {
        val name = tags["name"]?.let { " ($it)" } ?: ""
        //todo: Also add ref or piste:ref here?
        return arrayOf(name)
    }

    override fun createForm() = AddPisteDifficultyForm()

    override fun applyAnswerTo(answer: List<PisteDifficulty>, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["piste:difficulty"] = answer.joinToString(";") { it.osmValue }
    }
}
