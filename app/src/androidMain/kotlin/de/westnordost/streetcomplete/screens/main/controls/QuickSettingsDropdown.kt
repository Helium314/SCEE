package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.visiblequests.LevelFilter
import de.westnordost.streetcomplete.data.presets.EditTypePresetsController
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.screens.main.MainViewModel
import de.westnordost.streetcomplete.ui.common.DropdownMenuItem
import de.westnordost.streetcomplete.util.dialogs.LevelFilterDialog
import de.westnordost.streetcomplete.util.dialogs.showLevelFilterDialog
import de.westnordost.streetcomplete.util.showProfileSelectionDialog
import org.koin.compose.koinInject

@Composable
fun QuickSettingsDropdown(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
) {
    val editTypePresetsController: EditTypePresetsController = koinInject()
    val levelFilter: LevelFilter = koinInject()
    val prefs: Preferences = koinInject()
    val ctx = LocalContext.current
    var levelFilterDialog by remember { mutableStateOf(false) }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        DropdownMenuItem(onClick = {
            onDismissRequest()
            showProfileSelectionDialog(ctx, editTypePresetsController, prefs)
        })
        {
            Text(text = stringResource(R.string.quick_switch_preset))
        }
        DropdownMenuItem(onClick = { levelFilterDialog = true })
        {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.level_filter))
                Switch(levelFilter.isEnabled, { levelFilter.isEnabled = it; onDismissRequest() })
            }
        }
        DropdownMenuItem(onClick = {
                onDismissRequest()
                prefs.prefs.putString(Prefs.THEME_BACKGROUND, if (prefs.getString(Prefs.THEME_BACKGROUND, "MAP") == "MAP") "AERIAL" else "MAP")
            })
        {
            Text(text = stringResource(R.string.quick_switch_map_background))
        }
        DropdownMenuItem(onClick = {
            onDismissRequest()
            viewModel.reverseQuestOrder.value = !viewModel.reverseQuestOrder.value
        }) {
            val textResId = if (viewModel.reverseQuestOrder.collectAsState().value)
                R.string.quest_order_normal
            else R.string.quest_order_reverse
            Text(text = stringResource(textResId))
        }
    }
    if (levelFilterDialog)
        LevelFilterDialog(
            { onDismissRequest(); levelFilterDialog = false },
            viewModel.mapCamera.collectAsState().value
        )
}
