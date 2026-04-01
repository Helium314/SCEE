package de.westnordost.streetcomplete.osm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.access_manager_button_add
import de.westnordost.streetcomplete.resources.access_manager_button_add_conditional
import de.westnordost.streetcomplete.resources.access_manager_message
import de.westnordost.streetcomplete.resources.cancel
import de.westnordost.streetcomplete.resources.delete_confirmation
import de.westnordost.streetcomplete.resources.ic_delete_24
import de.westnordost.streetcomplete.resources.ok
import de.westnordost.streetcomplete.ui.common.DropdownButton
import de.westnordost.streetcomplete.ui.common.dialogs.SimpleListPickerDialog
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AccessManagerDialog(
    onDismissRequest: () -> Unit,
    tags: Map<String, String>,
    countryInfo: CountryInfo,
    onClickOk: (StringMapChangesBuilder) -> Unit
) {
    val originalAccessTags = tags.filterKeys { key -> accessKeys.any { it == key || key.startsWith("$it:") } }
    var newAccessTags by remember { mutableStateOf(originalAccessTags) }
    var showAddDialog by remember { mutableStateOf(false) }
    var addKey by remember { mutableStateOf<String?>(null) }
    var showAddConditionalDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        buttons = {
            Row(horizontalArrangement = Arrangement.End) {
                TextButton(onDismissRequest) { Text(stringResource(Res.string.cancel)) }
                TextButton(
                    onClick = {
                        val builder = StringMapChangesBuilder(tags)
                        newAccessTags.forEach {
                            if (originalAccessTags[it.key] != it.value)
                                builder[it.key] = it.value
                        }
                        originalAccessTags.keys.forEach {
                            if (it !in newAccessTags)
                                builder.remove(it)
                        }
                        onClickOk(builder)
                    },
                    enabled = originalAccessTags != newAccessTags
                ) {
                    Text(stringResource(Res.string.ok))
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(Res.string.access_manager_message))
                newAccessTags.forEach { (key, value) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(key, Modifier.weight(0.4f))
                        Spacer(Modifier.width(6.dp))
                        val values = if (value in accessValues) accessValues else (listOf(value) + accessValues)
                        DropdownButton(values, { newAccessTags += key to it }, Modifier.weight(0.4f), value, itemContent = { Text(it)} )
                        IconButton({ newAccessTags -= key }) { Icon(painterResource(Res.drawable.ic_delete_24), stringResource(Res.string.delete_confirmation)) }
                    }
                }
                Button({ showAddDialog = true }, Modifier.fillMaxWidth()) { Text(stringResource(Res.string.access_manager_button_add)) }
                Button({ showAddConditionalDialog = true }, Modifier.fillMaxWidth()) { Text(stringResource(Res.string.access_manager_button_add_conditional)) }
            }
        }
    )
    if (showAddDialog) {
        SimpleListPickerDialog(
            onDismissRequest =  { showAddDialog = false },
            items = accessKeys,
            onItemSelected = { addKey = it }
        )
    }
    if (addKey != null) {
        val key = addKey!!
        SimpleListPickerDialog(
            onDismissRequest =  { addKey = null },
            items = accessValues,
            onItemSelected = { newAccessTags += key to it }
        )
    }
    if (showAddConditionalDialog) {
        AddConditionalDialog(
            onDismissRequest = { showAddConditionalDialog = false },
            keys = accessKeys.toList(),
            values = listOf("yes", "no", "delivery", "destination"),
            numberOnly = true,
            countryInfo = countryInfo
        ) { key, value ->
            newAccessTags += key to value
            showAddConditionalDialog = false
        }
    }
}

val accessKeys = listOf( // sorted by number of uses
    "access", // 18m
    "foot", // 7m
    "bicycle", // 7m
    "bus", // 3.5m
    "motor_vehicle", // 2m
    "horse", // 1.6m
    "hgv", // 790k
    "motorcar", // 590k
    "motorcycle", // 580k
    "vehicle", // 350k
    "moped", // 235k
    "mofa", // 200k
    "golf_cart", // 158k
    "psv", // 115k
    "hazmat", // 87k
    "dog", // 80k
    "bdouble", // 60k
    "ski", // 60k
    "goods", // 41k
    "taxi", // 23k
    "carriage", // 20k
    "hov", // 20k
    "disabled", // 13.5k
    "tourist_bus", // 13k
    "atv", // 12k
    "hand_cart", // 6.8k
    "inline_skates", // 5k
    "speed_pedelec", // 3.7k
    "motorhome", // 3.5k
    "trailer", // 2.7k
    "ohv", // 2.4k
    "caravan", // 2k
    "coach", // 1.7k
    "carpool", // 1.5k
    "hgv_articulated", // 1k
    "small_electric_vehicle", // 800
    "auto_rickshaw", // 625
    "electric_bicycle", // 335
    "cycle_rickshaw", // 78
    "nev", // 62
    "kick_scooter", // 60
)

val accessValues = listOf(
    "yes",
    "no",
    "private",
    "permissive",
    "permit",
    "destination",
    "delivery",
    "customers",
    "designated", // not for access
    "use_sidepath", // usually for foot / bicycle
    "dismount", // bicycle
    "agricultural",
    "forestry",
    "discouraged", // really required explicit sign
    //"variable", doesn't make sense without supporting access:lanes
)
