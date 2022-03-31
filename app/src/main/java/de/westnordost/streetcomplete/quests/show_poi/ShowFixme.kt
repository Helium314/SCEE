package de.westnordost.streetcomplete.quests.show_poi

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.quests.singleTypeElementSelectionDialog

class ShowFixme(private val prefs: SharedPreferences) : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways, relations with
          (fixme or FIXME)
          and fixme !~ "${prefs.getString(PREF_FIXME_IGNORE, FIXME_IGNORE_DEFAULT)}"
          and FIXME !~ "${prefs.getString(PREF_FIXME_IGNORE, FIXME_IGNORE_DEFAULT)}"
    """
    override val changesetComment = "Remove fixme"
    override val wikiLink = "Key:fixme"
    override val icon = R.drawable.ic_quest_create_note
    override val dotColor = "red"
    override val defaultDisabledMessage = R.string.default_disabled_msg_poi_fixme

    override fun getTitle(tags: Map<String, String>) = R.string.quest_fixme_title

    override fun createForm() = ShowFixmeAnswerForm()

    override fun getTitleArgs(tags: Map<String, String>): Array<String>
        = arrayOf((tags["fixme"] ?: tags["FIXME"]).toString())

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        if (!answer) {
            tags.remove("fixme")
            tags.remove("FIXME")
        }
    }

    override val hasQuestSettings = true

    // actual ignoring of stuff happens when downloading
    override fun getQuestSettingsDialog(context: Context): AlertDialog =
        singleTypeElementSelectionDialog(context, prefs, PREF_FIXME_IGNORE, FIXME_IGNORE_DEFAULT, "Select fixme values to ignore")
}

private const val PREF_FIXME_IGNORE = "quest_fixme_ignore"
private const val FIXME_IGNORE_DEFAULT = "yes|continue|continue?"
