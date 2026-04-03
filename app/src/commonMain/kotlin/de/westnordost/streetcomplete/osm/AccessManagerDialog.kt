package de.westnordost.streetcomplete.osm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
import de.westnordost.streetcomplete.ui.common.dialogs.ScrollableAlertDialog
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
    val originalAccessTagsSets: Map<String, Set<String>> = remember(tags) {
        tags.filterKeys { key -> accessKeys.any { it == key || key.startsWith("$it:") } }
            .mapValues { entry -> parseValues(entry.value) }
    }

    val newAccessTags = remember(originalAccessTagsSets) {
        mutableStateMapOf<String, MutableSet<String>>().apply {
            originalAccessTagsSets.forEach { (key, values) ->
                put(key, values.toMutableSet())
            }
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var addKey by remember { mutableStateOf<String?>(null) }
    var showAddConditionalDialog by remember { mutableStateOf(false) }

    val hasChanges =
        normalizeAccessTags(originalAccessTagsSets) !=
            normalizeAccessTags(newAccessTags.mapValues { it.value.toSet() })

    val sortedEntries = newAccessTags.toSortedMap().entries.map { it.key to it.value.sorted() }

    ScrollableAlertDialog(
        onDismissRequest = onDismissRequest,
        content = {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    item {
                        Text(
                            text = stringResource(Res.string.access_manager_message),
                            style = MaterialTheme.typography.body1,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    itemsIndexed(sortedEntries) { index, (key, values) ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.subtitle1,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            values.forEach { value ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = value,
                                        style = MaterialTheme.typography.body1,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            val updated = newAccessTags[key]?.toMutableSet() ?: mutableSetOf()
                                            updated.remove(value)
                                            if (updated.isEmpty()) {
                                                newAccessTags.remove(key)
                                            } else {
                                                newAccessTags[key] = updated
                                            }
                                        }
                                    ) {
                                        Icon(
                                            painterResource(Res.drawable.ic_delete_24),
                                            stringResource(Res.string.delete_confirmation)
                                        )
                                    }
                                }
                            }

                            if (index != sortedEntries.lastIndex) {
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showAddDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(Res.string.access_manager_button_add))
                            }

                            Button(
                                onClick = { showAddConditionalDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(Res.string.access_manager_button_add_conditional))
                            }
                        }
                    }
                }
            }
        },
        buttons = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.cancel))
            }
            TextButton(
                onClick = {
                    val builder = StringMapChangesBuilder(tags)

                    newAccessTags.forEach { (key, values) ->
                        val joined = serializeValues(values)
                        val originalJoined = originalAccessTagsSets[key]?.let { serializeValues(it) }
                        if (joined != originalJoined) {
                            if (joined.isEmpty()) builder.remove(key) else builder[key] = joined
                        }
                    }

                    originalAccessTagsSets.keys.forEach { key ->
                        if (key !in newAccessTags || newAccessTags[key].isNullOrEmpty()) {
                            builder.remove(key)
                        }
                    }

                    onClickOk(builder)
                },
                enabled = hasChanges
            ) {
                Text(stringResource(Res.string.ok))
            }
        }
    )

    if (showAddDialog) {
        SimpleListPickerDialog(
            onDismissRequest = { showAddDialog = false },
            items = accessKeys,
            onItemSelected = { selectedKey ->
                addKey = selectedKey
                showAddDialog = false
            }
        )
    }

    addKey?.let { key ->
        AccessValuesDialog(
            title = key,
            values = accessValues,
            initialSelected = newAccessTags[key]?.toSet() ?: emptySet(),
            onDismissRequest = { addKey = null },
            onConfirm = { selected ->
                if (selected.isEmpty()) {
                    newAccessTags.remove(key)
                } else {
                    newAccessTags[key] = selected.toMutableSet()
                }
                addKey = null
            }
        )
    }

    if (showAddConditionalDialog) {
        AddConditionalDialog(
            onDismissRequest = { showAddConditionalDialog = false },
            keys = accessKeys,
            values = listOf("yes", "no", "delivery", "destination", "discouraged", "private"),
            numberOnly = false,
            countryInfo = countryInfo
        ) { key, value ->
            val updated = (newAccessTags[key]?.toMutableSet() ?: mutableSetOf())
            updated.add(value)
            newAccessTags[key] = updated
            showAddConditionalDialog = false
        }
    }
}

@Composable
private fun AccessValuesDialog(
    title: String,
    values: List<String>,
    initialSelected: Set<String>,
    onDismissRequest: () -> Unit,
    onConfirm: (Set<String>) -> Unit
) {
    val selectedStates = remember(values, initialSelected) {
        mutableStateMapOf<String, Boolean>().apply {
            values.forEach { value ->
                put(value, value in initialSelected)
            }
        }
    }

    ScrollableAlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.SemiBold
            )
        },
        content = {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(values) { value ->
                        val checked = selectedStates[value] == true
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .toggleable(
                                    value = checked,
                                    onValueChange = { isChecked ->
                                        selectedStates[value] = isChecked
                                    }
                                )
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { isChecked ->
                                    selectedStates[value] = isChecked
                                }
                            )
                            Text(
                                text = value,
                                style = MaterialTheme.typography.body1,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        buttons = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.cancel))
            }
            TextButton(
                onClick = {
                    val selected = selectedStates
                        .filterValues { it }
                        .keys
                        .toSet()
                    onConfirm(selected)
                }
            ) {
                Text(stringResource(Res.string.ok))
            }
        }
    )
}

private fun parseValues(value: String): Set<String> =
    value.split(';')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toSet()

private fun serializeValues(values: Set<String>): String =
    values.filter { it.isNotBlank() }
        .sorted()
        .joinToString(";")

private fun normalizeAccessTags(tags: Map<String, Set<String>>): Map<String, String> =
    tags.mapValues { (_, values) -> serializeValues(values) }
        .filterValues { it.isNotEmpty() }

// Access keys and values are used in multiple places (dialogs, overlays) - Usage figures as of February 2026
val accessKeys = listOf( // sorted by number of uses
    "access", // 25m
    "foot", // 13m
    "bicycle", // 9m
    "bus", // 4.8m
    "motor_vehicle", // 2.6m
    "horse", // 1.9m
    "hgv", // 1.5m
    "motorcycle", // 900k
    "motorcar", // 800k
    "vehicle", // 460k
    "mofa", // 318k
    "moped", // 317k
    "golf_cart", // 229k
    "hazmat", // 168k
    "dog", // 156k
    "psv", // 127k
    "snowmobile", // 117k
    "emergency", // 117k
    "mtb", // 88k
    "ski", // 70k
    "bdouble", // 60k
    "goods", // 53k
    "taxi", // 30k
    "carriage", // 22k
    "disabled", // 21k
    "hov", // 20k
    "atv", // 19k
    "tourist_bus", // 18k
    "trailer", // 12k
    "motorhome", // 10.9k
    "ohv", // 9.9k
    "hand_cart", // 7.6k
    "speed_pedelec", // 7.2k
    "inline_skates", // 6.8k
    "small_electric_vehicle", // 4.6k
    "coach", // 3.7k
    "caravan", // 2.8k
    "electric_bicycle", // 2k
    "carpool", // 1.9k
    "hgv_articulated", // 1.9k
    "auto_rickshaw", // 1.2k
    "kick_scooter", // 467
    "cycle_rickshaw", // 237
    "nev", // 66
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
    // "military", disputed tag
    //"variable", doesn't make sense without supporting access:lanes
)
