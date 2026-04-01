package de.westnordost.streetcomplete.osm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.osm.opening_hours.HierarchicOpeningHours
import de.westnordost.streetcomplete.osm.opening_hours.toOpeningHours
import de.westnordost.streetcomplete.ui.common.DropdownButton
import de.westnordost.streetcomplete.ui.common.dialogs.ScrollableAlertDialog
import de.westnordost.streetcomplete.ui.common.opening_hours.OpeningHoursTable
import de.westnordost.streetcomplete.ui.common.opening_hours.TimeMode

/**
 *  Allows selecting a key from [keys] and a value from [values] for a restriction like key=value @ (<conditional restrictions>).
 *  If [values] is null, a text field is shown instead of a selection dropdown, with the input restricted to [valueInputType].
 *  The conditional restrictions are weight, length, width and time.
 */
private val valueRegex = "[a-zA-Z0-9]+ @ \\(.+\\)".toRegex()
@Composable
fun AddConditionalDialog(
    onDismissRequest: () -> Unit,
    keys: List<String>,
    values: List<String>?,
    numberOnly: Boolean,
    countryInfo: CountryInfo,
    onClickOk: (String, String) -> Unit
) {
    var isValid by remember { mutableStateOf(false) }
    var fullValue by remember { mutableStateOf(TextFieldValue()) }
    var key by remember { mutableStateOf(keys.first()) }
    var value by remember { mutableStateOf(values?.first() ?: "") }
    var weight by remember { mutableStateOf(TextFieldValue("")) }
    var weightGtLt by remember { mutableStateOf("<") }
    var length by remember { mutableStateOf(TextFieldValue("")) }
    var lengthGtLt by remember { mutableStateOf("<") }
    var width by remember { mutableStateOf(TextFieldValue("")) }
    var widthGtLt by remember { mutableStateOf("<") }
    var hours by remember { mutableStateOf(HierarchicOpeningHours()) }
    val valueOptions = KeyboardOptions(keyboardType = if (numberOnly) KeyboardType.Number else KeyboardType.Text)

    LaunchedEffect(fullValue) {
        isValid = fullValue.text.matches(valueRegex) && fullValue.text.length < 255
    }
    LaunchedEffect(value, weight, weightGtLt, length, lengthGtLt, width, widthGtLt, hours) {
        val conditions = mutableListOf<String>()
        if (weight.text != "") conditions.add("weight $weightGtLt ${weight.text}")
        if (length.text != "") conditions.add("length $lengthGtLt ${length.text}")
        if (width.text != "") conditions.add("width $widthGtLt ${width.text}")
        if (hours.isComplete()) conditions.add(hours.toOpeningHours().toString())
        fullValue = TextFieldValue("$value @ (${conditions.joinToString(" AND ")})")
    }

    ScrollableAlertDialog(
        onDismissRequest = onDismissRequest,
        buttons = {
            TextButton(onDismissRequest) { Text(stringResource(R.string.cancel)) }
            TextButton({ onClickOk("$key:conditional", fullValue.text) }, enabled = isValid) {
                Text(stringResource(R.string.ok))
            }
        },
        content = {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), Arrangement.spacedBy(6.dp)) {
                // key dropdown
                DropdownButton(
                    items = keys,
                    onSelectedItem = { key = it },
                    modifier = Modifier.fillMaxWidth(),
                    selectedItem = keys.first(),
                    itemContent = { Text(it, Modifier.weight(1f)) }
                )
                // value dropdown or input field
                if (values != null)
                    DropdownButton(
                        items = values,
                        onSelectedItem = { value = it },
                        modifier = Modifier.fillMaxWidth(),
                        selectedItem = values.first(),
                        itemContent = { Text(it, Modifier.weight(1f)) }
                    )
                else
                    TextField(
                        value = value,
                        onValueChange = { value = it },
                        label = { Text("value") },
                        keyboardOptions = valueOptions
                    )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextField(
                        weight,
                        { weight = it },
                        label = { Text("weight") },
                        modifier = Modifier.weight(0.5f),
                        keyboardOptions = valueOptions
                    )
                    Text(weightGtLt)
                    Switch(weightGtLt != "<", { weightGtLt = if (it) ">" else "<" })
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextField(
                        length,
                        { length = it },
                        label = { Text("length") },
                        modifier = Modifier.weight(0.5f),
                        keyboardOptions = valueOptions
                    )
                    Text(lengthGtLt)
                    Switch(lengthGtLt != "<", { lengthGtLt = if (it) ">" else "<" })
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextField(
                        width,
                        { width = it },
                        label = { Text("width") },
                        modifier = Modifier.weight(0.5f),
                        keyboardOptions = valueOptions
                    )
                    Text(widthGtLt)
                    Switch(widthGtLt != "<", { widthGtLt = if (it) ">" else "<" })
                }
                OpeningHoursTable(
                    openingHours = hours,
                    onChange = { hours = it },
                    timeMode = TimeMode.Spans,
                    countryInfo = countryInfo,
                    addButtonContent = { Text(stringResource(R.string.quest_fee_add_times)) },
                    locale = countryInfo.userPreferredLocale,
                    userLocale = Locale.current,
                )
                Divider()
                TextField(fullValue, { fullValue = it })
            }
        }
    )
}
