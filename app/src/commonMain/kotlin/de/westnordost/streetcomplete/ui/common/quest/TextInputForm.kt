package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.ui.common.TextField2
import de.westnordost.streetcomplete.ui.theme.largeInput
import org.jetbrains.compose.resources.stringResource

@Composable
fun<T> TextInputForm(
    on: (QuestAction<T>) -> Unit,
    modifier: Modifier = Modifier,
    otherAnswers: @Composable () -> List<AnswerItem> = { emptyList() },
    isOk: (String) -> Boolean = { true },
    initialValue: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    hintText: String? = LocalQuestType.current!!.hint?.let { stringResource(it) },
    stringToAnswer: (String) -> T,
) {
    var text by remember { mutableStateOf(TextFieldValue(initialValue)) }

    QuestForm(
        on = on,
        isComplete = text.text.isNotBlank() && isOk(text.text),
        onClickOk = { on(Answer(stringToAnswer(text.text))) },
        modifier = modifier,
        otherAnswers = otherAnswers,
        hasChanges = text.text.isNotBlank(),
        hintText = hintText
    ) {
        ProvideTextStyle(MaterialTheme.typography.largeInput) {
            TextField2(
                value = text,
                onValueChange = { text = it },
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
            )
        }
    }
}

// for strings
@Composable
fun TextInputForm(
    on: (QuestAction<String>) -> Unit,
    modifier: Modifier = Modifier,
    otherAnswers: @Composable () -> List<AnswerItem> = { emptyList() },
    isOk: (String) -> Boolean = { true },
    initialValue: String = "",
    hintText: String? = LocalQuestType.current!!.hint?.let { stringResource(it) },
    keyboardType: KeyboardType = KeyboardType.Text
) {
    TextInputForm(on, modifier, otherAnswers, isOk, initialValue, keyboardType, hintText) { it }
}
