package de.westnordost.streetcomplete.quests.guidepost

import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.TextInputForm
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_guidepostName_hint
import org.jetbrains.compose.resources.stringResource

class AddGuidepostNameForm : TextInputForm<GuidepostNameAnswer>() {

    @Composable
    override fun hint() = stringResource(Res.string.quest_guidepostName_hint)
    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_placeName_no_name_answer) { confirmNoRef() }
    )

    override fun onClickOk() {
        applyAnswer(GuidepostName(text.value))
    }

    private fun confirmNoRef() {
        val ctx = context ?: return
        AlertDialog.Builder(ctx)
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(NoVisibleGuidepostName) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }
}
