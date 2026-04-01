package de.westnordost.streetcomplete.quests.address

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.address.HouseNumber
import de.westnordost.streetcomplete.ui.theme.extraLargeInput
import org.koin.compose.koinInject

/** Form to input a house number. */
@Composable
fun HouseNumberForm(
    value: HouseNumber,
    onValueChange: (HouseNumber) -> Unit,
    modifier: Modifier = Modifier,
    suggestion: HouseNumber? = null
) {
    val prefs: Preferences = koinInject()
    ProvideTextStyle(MaterialTheme.typography.extraLargeInput) {
        Row {
            HouseNumberInput(
                value = value.houseNumber,
                onValueChange = { onValueChange(HouseNumber(it, value.unit)) },
                modifier = modifier.weight(1f).widthIn(max = 256.dp),
                suggestion = suggestion?.houseNumber,
                label = ""
            )
            if (prefs.expertMode)
                HouseNumberInput(
                    value = value.unit ?: "",
                    onValueChange = {
                        val houseNumber = value.houseNumber.ifEmpty { suggestion?.houseNumber.orEmpty() }
                        onValueChange(HouseNumber(houseNumber, it.takeIf { it.isNotBlank() }))
                    },
                    modifier = modifier.weight(1f).widthIn(max = 256.dp),
                    suggestion = suggestion?.unit,
                    label = "unit"
                )
        }
    }
}
