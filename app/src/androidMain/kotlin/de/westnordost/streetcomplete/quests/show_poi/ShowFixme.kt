package de.westnordost.streetcomplete.quests.show_poi

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.questPrefix
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.quests.SingleTypeElementSelectionDialog
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_poi_fixme
import de.westnordost.streetcomplete.resources.quest_fixme_title

class ShowFixme : OsmFilterQuestType<Boolean>(), AndroidQuest {
    override val elementFilter = """
        nodes, ways, relations with
          (fixme or FIXME)
          and fixme !~ "${prefs.getString(questPrefix(prefs) + PREF_FIXME_IGNORE, FIXME_IGNORE_DEFAULT)}"
          and FIXME !~ "${prefs.getString(questPrefix(prefs) + PREF_FIXME_IGNORE, FIXME_IGNORE_DEFAULT)}"
    """
    override val changesetComment = "Remove/adjust fixme"
    override val wikiLink = "Key:fixme"
    override val icon = R.drawable.ic_quest_poi_fixme
    override val title = Res.string.quest_fixme_title
    override val dotColor = "red"
    override val defaultDisabledMessage = Res.string.default_disabled_msg_poi_fixme
    override val dotLabelSources = listOf("fixme", "FIXME")

    override fun createForm() = ShowFixmeAnswerForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (!answer) {
            tags.remove("fixme")
            tags.remove("FIXME")
        }
    }

    override val hasQuestSettings = true

    // actual ignoring of stuff happens when downloading
    @Composable
    override fun QuestSettings(onDismissRequest: () -> Unit) {
        SingleTypeElementSelectionDialog(prefs, questPrefix(prefs) + PREF_FIXME_IGNORE, FIXME_IGNORE_DEFAULT, R.string.quest_settings_fixme_title, onDismissRequest)
    }
}

private const val PREF_FIXME_IGNORE = "qs_ShowFixme_ignore_values"
private const val FIXME_IGNORE_DEFAULT = "yes|continue|continue?"
