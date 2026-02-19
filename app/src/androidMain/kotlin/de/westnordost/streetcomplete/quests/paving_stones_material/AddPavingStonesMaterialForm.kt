package de.westnordost.streetcomplete.quests.paving_stones_material

import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import kotlinx.serialization.serializer

class AddPavingStonesMaterialForm : AItemSelectQuestForm<PavingStonesMaterial, PavingStonesMaterial>() {

    override val items = PavingStonesMaterial.entries
    override val serializer = serializer<PavingStonesMaterial>()

    @Composable override fun ItemContent(item: PavingStonesMaterial) {
        ImageWithLabel(painterResource(item.imageResId), stringResource(item.titleResId))
    }

    override val itemsPerRow = 3

    override fun onClickOk(selectedItem: PavingStonesMaterial) {
        applyAnswer(selectedItem)
    }
}
