package de.westnordost.streetcomplete.quests.fence_material

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddFenceMaterialForm :
    AItemSelectQuestForm<FenceMaterial, FenceMaterial>() {

    override val items = FenceMaterial.entries
    override val serializer = serializer<FenceMaterial>()
    override val itemsPerRow = 3

    @Composable
    override fun ItemContent(item: FenceMaterial) {
        ImageWithLabel(
            painterResource(item.imageResId),
            stringResource(item.titleResId)
        )
    }

    override fun onClickOk(selectedItem: FenceMaterial) {
        applyAnswer(selectedItem)
    }
}
