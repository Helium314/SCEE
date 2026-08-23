package de.westnordost.streetcomplete.quests.show_poi

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.questPrefix
import de.westnordost.streetcomplete.quests.SingleTypeElementSelectionDialog
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

class ShowFixme : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways, relations with
          (fixme or FIXME)
          and fixme !~ "${prefs.getString(questPrefix(prefs) + PREF_FIXME_IGNORE, FIXME_IGNORE_DEFAULT)}"
          and FIXME !~ "${prefs.getString(questPrefix(prefs) + PREF_FIXME_IGNORE, FIXME_IGNORE_DEFAULT)}"
    """
    override val changesetComment = "Remove/adjust fixme"
    override val wikiLink = "Key:fixme"
    override val icon = Res.drawable.ic_quest_poi_fixme
    override val title = Res.string.quest_fixme_title
    override val dotColor = "red"
    override val defaultDisabledMessage = Res.string.default_disabled_msg_poi_fixme
    override val dotLabelSources = listOf("fixme", "FIXME")

    @Composable
    override fun Form(on: (QuestAction<Boolean>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        QuestForm(on, answers = listOf(AnswerItem(stringResource(Res.string.quest_fixme_remove)) { on(Answer(false)) }))
    }

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
        SingleTypeElementSelectionDialog(prefs, questPrefix(prefs) + PREF_FIXME_IGNORE, FIXME_IGNORE_DEFAULT, Res.string.quest_settings_fixme_title, onDismissRequest)
    }
}

private const val PREF_FIXME_IGNORE = "qs_ShowFixme_ignore_values"
private const val FIXME_IGNORE_DEFAULT = "yes|continue|continue?"
