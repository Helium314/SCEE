package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.ScrollableAlertDialog
import de.westnordost.streetcomplete.ui.theme.largeInput
import de.westnordost.streetcomplete.util.locale.DateFormatElements
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

/** Dialog in which to select a date */
@Composable
fun DateSelectDialog(
    onDismissRequest: () -> Unit,
    onSelect: (date: LocalDate) -> Unit,
    initialDate: LocalDate,
    modifier: Modifier = Modifier,
    title: (@Composable () -> Unit)? = null,
    years: IntRange = (initialDate.year - 10)..(initialDate.year + 10),
    locale: Locale? = null,
    dismissOnSelect: Boolean = true,
    text: (@Composable () -> Unit)? = null,
    neutralButton: (@Composable () -> Unit)? = null,
) {
    val dateFormatElements = remember(locale) { DateFormatElements.of(locale) }
    val datePickerState = rememberDatePickerState(
        initialDate = initialDate,
        years = years,
    )

    ScrollableAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = title,
        content = {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                Box(Modifier.fillMaxWidth()) {
                    ProvideTextStyle(MaterialTheme.typography.largeInput) {
                        DatePicker(
                            state = datePickerState,
                            dateFormatElements = dateFormatElements,
                            modifier = Modifier.align(Alignment.Center),
                            locale = locale,
                            visibleAdjacentItems = 2,
                        )
                    }
                }
            }
            if (text != null) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    ProvideTextStyle(MaterialTheme.typography.body2) {
                        Box(Modifier.padding(top = 16.dp, start = 24.dp, end = 24.dp).fillMaxWidth()) {
                            text()
                        }
                    }
                }
            }
        },
        buttonRow = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.cancel))
            }
            if (neutralButton != null) {
                neutralButton()
            }
            TextButton(
                onClick = {
                    onSelect(datePickerState.date)
                    if (dismissOnSelect) onDismissRequest()
                }
            ) {
                Text(stringResource(Res.string.ok))
            }
        }
    )
}
