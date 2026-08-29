package de.westnordost.streetcomplete.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.RadioGroup
import de.westnordost.streetcomplete.ui.common.TextField2
import de.westnordost.streetcomplete.ui.common.dialogs.AlertDialog
import de.westnordost.streetcomplete.ui.common.dialogs.TextInputDialog
import de.westnordost.streetcomplete.ui.common.dialogs.WheelPickerDialog
import de.westnordost.streetcomplete.ui.common.settings.Preference
import de.westnordost.streetcomplete.ui.common.settings.SwitchPreference
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun UiSettingsScreen(
    onClickBack: () -> Unit,
) {
    val prefs: Preferences = koinInject()
    var showMinLinesDialog by remember { mutableStateOf(false) }
    var showRotateAngleDialog by remember { mutableStateOf(false) }
    var showNearbyQuestDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(Res.string.pref_screen_ui)) },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
        )
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                    )
                )
        ) {
            SwitchPreference(
                name = stringResource(Res.string.pref_show_quick_settings_title),
                pref = Prefs.QUICK_SETTINGS,
                default = false,
            )
            SwitchPreference(
                name = stringResource(Res.string.pref_overlay_quick_selector_title),
                pref = Prefs.OVERLAY_QUICK_SELECTOR,
                default = false,
            )
            SwitchPreference(
                name = stringResource(Res.string.pref_show_next_quest_title),
                description = stringResource(Res.string.pref_show_next_quest_summary),
                pref = Prefs.SHOW_NEXT_QUEST_IMMEDIATELY,
                default = false,
            )
            Preference(
                name = stringResource(Res.string.pref_show_nearby_quests_title),
                onClick = { showNearbyQuestDialog = true },
                description = stringResource(Res.string.pref_show_nearby_quests_summary)
            )
            SwitchPreference(
                name = stringResource(Res.string.pref_hide_button_title),
                description = stringResource(Res.string.pref_hide_button_summary),
                pref = Prefs.SHOW_HIDE_BUTTON,
                default = false,
            )
            SwitchPreference(
                name = stringResource(Res.string.pref_create_node_show_keyboard_title),
                pref = Prefs.CREATE_NODE_SHOW_KEYBOARD,
                default = true,
            )
            SwitchPreference(
                name = stringResource(Res.string.pref_select_first_edit_title),
                description = stringResource(Res.string.pref_select_first_edit_summary),
                pref = Prefs.SELECT_FIRST_EDIT,
                default = true,
            )
            SwitchPreference(
                name = stringResource(Res.string.pref_search_more_languages_title),
                description = stringResource(Res.string.pref_search_more_languages_summary),
                pref = Prefs.SEARCH_MORE_LANGUAGES,
                default = false,
            )
            Preference(
                name = stringResource(Res.string.pref_recent_answers_first_min_lines),
                onClick = { showMinLinesDialog = true },
                description = stringResource(Res.string.pref_recent_answers_first_min_lines_summary, prefs.getInt(Prefs.FAVS_FIRST_MIN_LINES, 1))
            )
            SwitchPreference(
                name = stringResource(Res.string.pref_disable_navigation_mode_title),
                description = stringResource(Res.string.pref_disable_navigation_mode_summary),
                pref = Prefs.DISABLE_NAVIGATION_MODE,
                default = false,
            )
            SwitchPreference(
                name = stringResource(Res.string.pref_main_menu_grid),
                pref = Prefs.MAIN_MENU_FULL_GRID,
                default = false,
            )
            SwitchPreference(
                name = stringResource(Res.string.pref_main_menu_switch_presets_title),
                pref = Prefs.MAIN_MENU_SWITCH_PRESETS,
                default = false,
            )
            SwitchPreference(
                name = stringResource(Res.string.pref_caps_word_name_input),
                pref = Prefs.CAPS_WORD_NAME_INPUT,
                default = false,
            )
            SwitchPreference(
                name = stringResource(Res.string.pref_volume_zoom_title),
                description = stringResource(Res.string.pref_volume_zoom_summary),
                pref = Prefs.VOLUME_ZOOM,
                default = false,
            )
            SwitchPreference(
                name = stringResource(Res.string.pref_rotate_while_zooming_title),
                pref = Prefs.ROTATE_WHILE_ZOOMING,
                default = false,
            )
            Preference(
                name = stringResource(Res.string.pref_rotate_angle_threshold_title),
                onClick = { showRotateAngleDialog = true },
            )
        }
    }
    if (showMinLinesDialog) {
        val selectable = remember { (0..10).toList() }
        WheelPickerDialog(
            onDismissRequest = { showMinLinesDialog = false },
            selectableValues = selectable,
            onSelected = { prefs.putInt(Prefs.FAVS_FIRST_MIN_LINES, it) },
            itemContent = { Text(it.toString()) },
            selectedInitialValue = prefs.getInt(Prefs.FAVS_FIRST_MIN_LINES, 1),
            title = { Text(stringResource(Res.string.pref_recent_answers_first_min_lines)) },
            text = { Text(stringResource(Res.string.pref_recent_answers_first_min_lines_message)) },
        )
    }
    if (showRotateAngleDialog)
        TextInputDialog(
            onDismissRequest = { showRotateAngleDialog = false },
            onConfirmed = { prefs.putFloat(Prefs.ROTATE_ANGLE_THRESHOLD, it.toFloatOrNull() ?: 1.5f) },
            text = prefs.getFloat(Prefs.ROTATE_ANGLE_THRESHOLD, 1.5f).toString(),
            title = { Text(stringResource(Res.string.pref_rotate_angle_threshold_title)) },
            //textInputLabel = { Text(stringResource(Res.string.pref_search_more_languages_summary)) },
            keyboardType = KeyboardType.Decimal,
            checkTextValid = {
                val value = it.toFloatOrNull()
                value != null && value >= 0
            }
        )
    if (showNearbyQuestDialog) {
        val items = remember { listOfNotNull(
            0 to Res.string.show_nearby_quests_disable,
            1 to Res.string.show_nearby_quests_visible,
            if (prefs.expertMode) 2 to Res.string.show_nearby_quests_all_types else null,
            if (prefs.expertMode) 3 to Res.string.show_nearby_quests_even_hidden else null
        ) }
        var selected by remember { mutableIntStateOf(prefs.getInt(Prefs.SHOW_NEARBY_QUESTS, 0)) }
        var distance by remember { mutableStateOf(TextFieldValue(prefs.getFloat(Prefs.SHOW_NEARBY_QUESTS_DISTANCE, 0.0f).toString())) }
        AlertDialog(
            onDismissRequest = { showNearbyQuestDialog = false },
            title = { Text(stringResource(Res.string.pref_show_nearby_quests_title)) },
            buttonRow = {
                TextButton({ showNearbyQuestDialog = false }) { Text(stringResource(Res.string.cancel)) }
                TextButton({
                    prefs.putFloat(Prefs.SHOW_NEARBY_QUESTS_DISTANCE, (distance.text.toFloatOrNull() ?: 0f)
                        .coerceAtLeast(0.0f).coerceAtMost(10.0f))
                    showNearbyQuestDialog = false
                }) { Text(stringResource(Res.string.ok)) }
            },
            text = {
                Column {
                    RadioGroup(
                        items,
                        { selected = it.first },
                        selectedOption = items.getOrNull(selected),
                        itemContent = { Text(stringResource(it.second)) }
                    )
                    Text(stringResource(Res.string.show_nearby_quests_distance))
                    TextField2(distance, { distance = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                }
            }
        )
    }
}
