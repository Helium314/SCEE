package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import de.westnordost.streetcomplete.ApplicationConstants.MAX_OSM_TAG_VALUE_LENGTH
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.preferences.addLastPicked
import de.westnordost.streetcomplete.data.preferences.getLastPicked
import de.westnordost.streetcomplete.ui.common.auto_complete_text.AutoCompleteTextField
import de.westnordost.streetcomplete.ui.theme.largeInput
import de.westnordost.streetcomplete.util.takeFavorites
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import kotlin.collections.orEmpty

@Composable
fun MultiValueQuestForm(
    on: (QuestAction<String>) -> Unit,
    addAnotherValueText: StringResource,
    modifier: Modifier = Modifier,
    isOk: (String) -> Boolean = { true },
    simpleSuggestions: Collection<String>? = null,
    prioritySuggestions: (String) -> Collection<String>? = { null },
    minLengthForSuggestions: Int = 1,
    otherAnswers: @Composable (() -> List<AnswerItem>) = { emptyList() },
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    hint: String? = null
) {
    var values by rememberSaveable { mutableStateOf(setOf<String>()) }
    var currentValue by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }
    val isTooLong by remember { derivedStateOf { values.sumOf { it.length + 1 } + currentValue.text.length > MAX_OSM_TAG_VALUE_LENGTH } }
    val prefs: Preferences = koinInject()
    val questType = LocalQuestType.current!!

    QuestForm(
        on = on,
        isComplete = ((currentValue.text.isNotBlank() && isOk(currentValue.text)) || values.isNotEmpty()) && !isTooLong,
        onClickOk = {
            val vals = if (currentValue.text.isBlank()) values else values + currentValue.text.trim()
            vals.forEach { prefs.addLastPicked(questType.name, it) }
            on(Answer(vals.joinToString(";")))
        },
        modifier = modifier,
        otherAnswers = otherAnswers,
    ) {
        Column {
            Text(values.joinToString(";")) // todo: actually it should be easy to let the user select a value to change it
            AutoCompleteTextField(
                value = currentValue,
                onValueChange = { currentValue = it },
                suggestions = prioritySuggestions(currentValue.text).orEmpty() +
                    prefs.getLastPicked<String>(questType.name).takeFavorites(20, 50, 1) +
                    simpleSuggestions
                        ?.takeIf { currentValue.text.length >= minLengthForSuggestions }
                        ?.filter { it.startsWith(currentValue.text, ignoreCase = true) }
                        .orEmpty(),
                textStyle = MaterialTheme.typography.largeInput,
                isError = isTooLong,
                keyboardOptions = keyboardOptions,
                placeholder = hint?.let { { Text(it) } },
                startExpanded = true,
                startExpandedWithoutFocus = true
            )
            TextButton(
                onClick = { values = values + currentValue.text.trim(); currentValue = TextFieldValue() }, // todo: show dropdown if minLengthForSuggestions is 0, also on start
                enabled = currentValue.text.isNotBlank() && isOk(currentValue.text) && currentValue.text.trim() !in values
            ) {
                Text(stringResource(addAnotherValueText))
            }
        }
    }
}
