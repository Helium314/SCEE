package de.westnordost.streetcomplete.screens.main.overlays

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.overlays.custom.CustomOverlay
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.custom_overlay_add_button
import de.westnordost.streetcomplete.resources.overlay_none
import de.westnordost.streetcomplete.ui.common.DropdownMenuItem
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import de.westnordost.streetcomplete.resources.ic_settings_48
import de.westnordost.streetcomplete.resources.ic_add_24
import de.westnordost.streetcomplete.util.OverlayCustomizer
import de.westnordost.streetcomplete.util.fakeStringResource
import de.westnordost.streetcomplete.util.getCustomOverlayIndices
import org.koin.compose.koinInject

/** Dropdown menu for selecting an overlay */
@Composable
fun OverlaySelectionDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    overlays: List<Overlay>,
    onSelect: (Overlay?) -> Unit,
    modifier: Modifier = Modifier
) {
    val questTypeRegistry: QuestTypeRegistry = koinInject()
    val prefs: Preferences = koinInject()
    var showOverlayCustomizer by rememberSaveable { mutableStateOf<Int?>(null) }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        DropdownMenuItem(onClick = { onDismissRequest(); onSelect(null) }) {
            Text(
                text = stringResource(Res.string.overlay_none),
                modifier = Modifier.padding(start = 48.dp)
            )
        }
        for (overlay in overlays) {
            DropdownMenuItem(onClick = { onDismissRequest(); onSelect(overlay) }) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(overlay.icon),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = if (overlay.title != fakeStringResource) stringResource(overlay.title) else overlay.changesetComment,
                        modifier = Modifier.weight(1f)
                    )
                    if (overlay.title == fakeStringResource) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_settings_48),
                            contentDescription = null,
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { showOverlayCustomizer = overlay.wikiLink!!.toInt() }
                        )
                    }
                }
            }
        }
        if (prefs.expertMode) {
            DropdownMenuItem(onClick = {
                showOverlayCustomizer = (getCustomOverlayIndices(prefs).maxOrNull() ?: 0) + 1
            }) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_add_24),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = stringResource(Res.string.custom_overlay_add_button),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        if (showOverlayCustomizer != null)
            OverlayCustomizer(
                onDismiss = { onDismissRequest(); showOverlayCustomizer = null },
                index = showOverlayCustomizer!!,
                { prefs.selectedOverlayName = CustomOverlay::class.simpleName }, // not great, as it relies on onSelected not changing
                onDeleted = { if (it) onSelect(null) },
                questTypeRegistry = questTypeRegistry
            )
    }
}
