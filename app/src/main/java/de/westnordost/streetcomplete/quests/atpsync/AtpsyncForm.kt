package de.westnordost.streetcomplete.quests.atpsync

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractExternalSourceQuestForm
import de.westnordost.streetcomplete.data.quest.ExternalSourceQuestKey
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.screens.main.MainFragment
import de.westnordost.streetcomplete.screens.main.bottom_sheet.CreatePoiFragment
import org.koin.android.ext.android.inject

@SuppressLint("SetTextI18n") // android studio complains, but that's element type and id and probably should not be translated
class AtpsyncForm : AbstractExternalSourceQuestForm() {

    private val atpsyncDao: AtpsyncDao by inject()

    private val issue: AtpsyncMissingLocation? by lazy {
        val key = questKey as ExternalSourceQuestKey
        atpsyncDao.getMissingLocation(key.id)
    }

    override val buttonPanelAnswers by lazy {
        val tags = issue!!.tags
        val position = issue!!.position
        listOfNotNull(
            AnswerItem(R.string.quest_custom_quest_add_node) {
                val fragment = CreatePoiFragment.createWithPrefill(tags, position, questKey)
                parentFragmentManager.commit {
                    add(id, fragment, null)
                    addToBackStack(null)
                }
                (parentFragment as? MainFragment)?.offsetPos(position)
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tags = issue!!.tags
        val displayName = tags["name"] ?: tags["brand"]!!
        setTitle(requireContext().resources.getString(R.string.quest_atpsync_title, displayName))
    }
}
