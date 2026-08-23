package de.westnordost.streetcomplete.ui.common

import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.background_type_aerial_esri
import de.westnordost.streetcomplete.resources.background_type_map
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun SwitchMapBackgroundButton() {
    val prefs: Preferences = koinInject()
    var text by remember {
        mutableStateOf(if (prefs.getString(Prefs.THEME_BACKGROUND, "MAP") == "MAP")
            Res.string.background_type_aerial_esri
        else Res.string.background_type_map)
    }
    fun toggleBackground() {
        val isMap = prefs.getString(Prefs.THEME_BACKGROUND, "MAP") == "MAP"
        prefs.putString(Prefs.THEME_BACKGROUND, if (isMap) "AERIAL" else "MAP")
        text = if (isMap) Res.string.background_type_map else Res.string.background_type_aerial_esri
    }
    TextButton(::toggleBackground) { Text(stringResource(text)) }
}
