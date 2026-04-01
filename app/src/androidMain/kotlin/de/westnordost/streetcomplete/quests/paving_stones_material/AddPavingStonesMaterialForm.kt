package de.westnordost.streetcomplete.quests.paving_stones_material

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddPavingStonesMaterialForm : AItemSelectQuestForm<PavingStonesMaterial, PavingStonesMaterialAnswer>() {

    override val items = PavingStonesMaterial.entries
    override val itemsPerRow = 3
    override val serializer = serializer<PavingStonesMaterial>()

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_smoothness_wrong_surface) {
            applyAnswer(SurfaceIsNotPavingStones)
        },
    )

    @Composable override fun ItemContent(item: PavingStonesMaterial) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItem: PavingStonesMaterial) {
        applyAnswer(selectedItem)
    }
}
