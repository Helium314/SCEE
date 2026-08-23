package de.westnordost.streetcomplete.screens.main.overlays

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlendModeColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.overlays.custom.CustomOverlay
import de.westnordost.streetcomplete.util.OverlayCustomizer
import de.westnordost.streetcomplete.util.fakeStringResource
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun OverlayQuickSelector(
    overlays: List<Overlay>,
    selectedOverlay: Overlay?,
    modifier: Modifier,
    onOverlaySelected: (Overlay?) -> Unit
) {
    val questTypeRegistry: QuestTypeRegistry = koinInject()
    val prefs: Preferences = koinInject()
    val scroll = rememberScrollState()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    var showOverlayCustomizer by rememberSaveable { mutableStateOf<Int?>(null) }
    LaunchedEffect(selectedOverlay) {
        scope.launch { bringIntoViewRequester.bringIntoView() }
    }
    Column(
        modifier = modifier.heightIn(max = 330.dp).width(48.dp).verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        overlays.forEach {
            // weird equality because of custom overlays
            val selected = if (selectedOverlay !is CustomOverlay) it == selectedOverlay
                else it.wikiLink?.toIntOrNull() == prefs.getInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, 0)
            val modifier = if (!selected) Modifier
            else Modifier.border(3.dp, colorResource(R.color.quest_selection_frame), CircleShape)
                .bringIntoViewRequester(bringIntoViewRequester)
            Box(modifier) {
                Image(
                    painter = painterResource(it.icon),
                    contentDescription = if (it.title == fakeStringResource) it.wikiLink else stringResource(it.title),
                    modifier = Modifier.size(48.dp).combinedClickable(
                        onClick = { onOverlaySelected(if (selected) null else it) },
                        onLongClick = {
                            if (it.title != fakeStringResource) return@combinedClickable // only for custom overlays
                            val index = it.wikiLink?.toIntOrNull() ?: return@combinedClickable
                            showOverlayCustomizer = index
                        }
                    ),
                    colorFilter = if (selected) null else BlendModeColorFilter(Color.Gray, BlendMode.Modulate),
                )
            }
        }
    }
    if (showOverlayCustomizer != null)
        OverlayCustomizer(
            onDismiss = { showOverlayCustomizer = null },
            index = showOverlayCustomizer!!,
            onChanged = { },
            onDeleted = { if (it) onOverlaySelected(null) },
            questTypeRegistry = questTypeRegistry
        )
}
