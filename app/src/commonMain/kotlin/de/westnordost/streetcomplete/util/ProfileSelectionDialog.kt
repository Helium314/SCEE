package de.westnordost.streetcomplete.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.presets.EditTypePreset
import de.westnordost.streetcomplete.data.presets.EditTypePresetsController
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_presets_default_name
import de.westnordost.streetcomplete.resources.quest_settings_per_preset_rescan
import de.westnordost.streetcomplete.ui.common.ToastPopup
import de.westnordost.streetcomplete.ui.common.dialogs.SimpleListPickerDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable fun ProfileSelectionDialog(
    onDismissRequest: () -> Unit,
) {
    val editTypePresetsController: EditTypePresetsController = koinInject()
    val prefs: Preferences = koinInject()
    val presets = mutableListOf<EditTypePreset>()
    presets.add(EditTypePreset(0, stringResource(Res.string.quest_presets_default_name)))
    presets.addAll(editTypePresetsController.getAll())
    var selected = -1
    (presets).forEachIndexed { index, questPreset ->
        if (questPreset.id == editTypePresetsController.selectedId)
            selected = index
    }
    var showRescanMessage by remember { mutableStateOf(false) }
    SimpleListPickerDialog(
        onDismissRequest,
        presets,
        {
            if (prefs.getBoolean(Prefs.QUEST_SETTINGS_PER_PRESET, false)) {
                OsmQuestController.reloadQuestTypes()
                if (!prefs.getBoolean(Prefs.DYNAMIC_QUEST_CREATION, false))
                    showRescanMessage = true
            }
            // launch in background, because this can block for quite a while if database is occupied
            GlobalScope.launch(Dispatchers.IO) { editTypePresetsController.selectedId = it.id }
        },
        selectedItem = presets.getOrNull(selected),
        getItemName = { it.name }
    )
    if (showRescanMessage)
        ToastPopup(
            onDismissRequest = { showRescanMessage = false},
            text = stringResource(Res.string.quest_settings_per_preset_rescan),
            isInDialog = true
        )
}
