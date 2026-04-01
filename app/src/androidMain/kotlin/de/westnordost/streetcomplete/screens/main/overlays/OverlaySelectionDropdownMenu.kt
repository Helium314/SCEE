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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.overlays.custom.CustomOverlay
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.overlay_none
import de.westnordost.streetcomplete.ui.common.DropdownMenuItem
import de.westnordost.streetcomplete.util.showOverlayCustomizer
import org.koin.compose.koinInject
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource
import de.westnordost.streetcomplete.resources.ic_settings_48
import de.westnordost.streetcomplete.resources.ic_add_24
import de.westnordost.streetcomplete.util.fakeStringResource
import de.westnordost.streetcomplete.util.getCustomOverlayIndices

/** Dropdown menu for selecting an overlay */
@Composable
fun OverlaySelectionDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    overlays: List<Overlay>,
    onSelect: (Overlay?) -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val questTypeRegistry: QuestTypeRegistry = koinInject()
    val prefs: Preferences = koinInject()

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
                                .clickable {
                                    onDismissRequest()
                                    showOverlayCustomizer(overlay.wikiLink!!.toInt(), ctx, prefs, questTypeRegistry,
                                        { onSelect(overlay) },
                                        { if (it) onSelect(null) }
                                    )
                                }
                        )
                    }
                }
            }
        }
        if (prefs.expertMode) {
            DropdownMenuItem(onClick = {
                onDismissRequest()
                showOverlayCustomizer((getCustomOverlayIndices(prefs).maxOrNull() ?: 0) + 1, ctx, prefs, questTypeRegistry,
                    { prefs.selectedOverlayName = CustomOverlay::class.simpleName }, // not great, as it relies on onSelected not changing
                    { onSelect(null) }
                )
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
                        text = stringResource(R.string.custom_overlay_add_button),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
