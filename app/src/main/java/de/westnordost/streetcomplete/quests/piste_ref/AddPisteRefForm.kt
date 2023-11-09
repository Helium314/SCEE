package de.westnordost.streetcomplete.quests.piste_ref

import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestRefBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull

class AddPisteRefForm : AbstractOsmQuestForm<PisteRefAnswer>() {

    //override val contentLayoutResId = R.layout.view_piste_ref
    override val contentLayoutResId = R.layout.quest_ref
    private val binding by contentViewBinding(QuestRefBinding::bind)

    override val otherAnswers get() =
        listOfNotNull(
            AnswerItem(R.string.quest_piste_ref_connection) { confirmPisteConnection() }
        )

    private val ref get() = binding.refInput.nonBlankTextOrNull

    override fun onClickOk() {
        applyAnswer(PisteRef(ref!!))
    }

    private fun confirmPisteConnection() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(
                PisteConnection
            ) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    override fun isFormComplete() = ref != null
}
