package de.westnordost.streetcomplete.quests.street_cabinet

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.getSystemResourceEnvironment

class AddStreetCabinetTypeForm : AItemSelectQuestForm<StreetCabinetType, StreetCabinetType>() {

    override val items = StreetCabinetType.entries
    override val itemsPerRow = 4
    override val moveFavoritesToFront = false
    override val serializer = serializer<StreetCabinetType>()

    @Composable override fun ItemContent(item: StreetCabinetType) {
        ImageWithLabel(painterResource(item.iconResId), stringResource(item.titleResId))
    }

    override fun getTitleString() = element.tags["operator"]?.let { runBlocking {
        org.jetbrains.compose.resources.getString(getSystemResourceEnvironment(), questType.title) + " ($it)"
    } }

    override fun onClickOk(selectedItem: StreetCabinetType) {
        applyAnswer(selectedItem)
    }
}
