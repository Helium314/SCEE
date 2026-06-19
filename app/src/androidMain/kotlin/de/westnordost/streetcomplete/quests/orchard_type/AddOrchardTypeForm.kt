package de.westnordost.streetcomplete.quests.orchard_type

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddOrchardTypeForm : AItemSelectQuestForm<OrchardType, OrchardType>() {

    override val items = OrchardType.entries
    override val itemsPerRow = 1
    override val serializer = serializer<OrchardType>()

    @Composable override fun ItemContent(item: OrchardType) {
        ImageWithDescription(
            painter = painterResource(item.icon),
            title = stringResource(item.title),
            description = stringResource(item.description),
            imageSize = DpSize(64.dp, 64.dp)
        )
    }

    override fun onClickOk(selectedItem: OrchardType) {
        applyAnswer(selectedItem)
    }
}
