package de.westnordost.streetcomplete.quests.kerb_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddKerbTypeForm : AItemSelectQuestForm<KerbType, KerbType>() {

    override val items = KerbType.entries
    override val itemsPerRow = 2
    override val serializer = serializer<KerbType>()
    override val moveFavoritesToFront = false

    @Composable override fun ItemContent(item: KerbType) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItem: KerbType) {
        applyAnswer(selectedItem)
    }
}
