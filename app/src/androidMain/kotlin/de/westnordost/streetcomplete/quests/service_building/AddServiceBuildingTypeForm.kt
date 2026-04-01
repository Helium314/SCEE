package de.westnordost.streetcomplete.quests.service_building

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AGroupedItemSelectQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.getSystemResourceEnvironment

class AddServiceBuildingTypeForm : AGroupedItemSelectQuestForm<ServiceBuildingTypeCategory, ServiceBuildingType, ServiceBuildingType>() {

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_disused) { applyAnswer(ServiceBuildingType.DISUSED) }
    )

    override val topItems = listOf(
        ServiceBuildingType.MINOR_SUBSTATION,
        ServiceBuildingType.GAS_PRESSURE_REGULATION,
        ServiceBuildingType.VENTILATION_SHAFT,
        ServiceBuildingType.WATER_WELL,
        ServiceBuildingType.HEATING,
    )

    override val groups = ServiceBuildingTypeCategory.entries

    override val serializer = serializer<ServiceBuildingType>()

    override fun getTitleString() = element.tags["operator"]?.let { runBlocking {
        org.jetbrains.compose.resources.getString(getSystemResourceEnvironment(), questType.title) + " ($it)"
    } }

    @Composable override fun GroupContent(item: ServiceBuildingTypeCategory) {
        ImageWithDescription(
            painter = painterResource(item.iconResId),
            title = stringResource(item.titleResId),
            description = null,
            imageSize = DpSize(48.dp, 48.dp)
        )
    }

    @Composable override fun ItemContent(item: ServiceBuildingType) {
        ImageWithDescription(
            painter = painterResource(item.iconResId),
            title = stringResource(item.titleResId),
            description = item.descriptionResId?.let { stringResource(it) },
            imageSize = DpSize(48.dp, 48.dp),
        )
    }

    override fun onClickOk(value: ServiceBuildingType) {
        applyAnswer(value)
    }
}
