package de.westnordost.streetcomplete.quests.tree

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.ARadioGroupQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import org.jetbrains.compose.resources.stringResource

class AddTreeDenotationForm : ARadioGroupQuestForm<TreeDenotation, TreeDenotationAnswer>() {

    override val items = TreeDenotation.entries

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_leafType_tree_is_just_a_stump) {
            applyAnswer(NotTreeButStump, true)
        },
    )

    @Composable override fun BoxScope.ItemContent(item: TreeDenotation) {
        ImageWithDescription(
            painter = null,
            title = stringResource(item.title),
            description = stringResource(item.description),
        )
    }
}
