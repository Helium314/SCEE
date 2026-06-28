package de.westnordost.streetcomplete.quests.general_ref

import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.TextInputForm
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_generalRef_hint
import de.westnordost.streetcomplete.resources.quest_guidepostRef_hint
import org.jetbrains.compose.resources.stringResource

class AddGeneralRefForm : TextInputForm<GeneralRefAnswer>() {
    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_ref_answer_noRef) { confirmNoRef() }
    )

    @Composable
    override fun hint(): String{
        return if (element.tags.containsKey("guidepost") || element.tags["information"] == "guidepost")
            stringResource(Res.string.quest_guidepostRef_hint)
        else
            stringResource(Res.string.quest_generalRef_hint)
    }

    override fun onClickOk() {
        applyAnswer(GeneralRef(text.value))
    }

    private fun confirmNoRef() {
        val ctx = context ?: return
        AlertDialog.Builder(ctx)
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(NoVisibleGeneralRef) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }
}
