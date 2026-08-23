package de.westnordost.streetcomplete.quests.wheelchair_access

import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.quests.wheelchair_access.WheelchairAccess.*
import de.westnordost.streetcomplete.ui.common.TextField2
import de.westnordost.streetcomplete.ui.common.dialogs.ScrollableAlertDialog
import de.westnordost.streetcomplete.ui.common.quest.LocalElement
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddWheelchairAccessForm(
    on: (QuestAction<WheelchairAccess>) -> Unit,
    countryInfo: CountryInfo
) {
    val element = LocalElement.current!!
    var descriptions by rememberSaveable { mutableStateOf(
        (countryInfo.officialLanguages.map { ":$it" } + ":en" + "").toSet().associateWith {
            element.tags["wheelchair:description$it"] ?: ""
        }
    ) }
    var showDescriptionDialog by rememberSaveable { mutableStateOf(false) }
    QuestForm(
        on = on,
        answers = listOf(
            AnswerItem(stringResource(Res.string.quest_generic_hasFeature_no)) { on(Answer(NO.apply { updatedDescriptions = descriptions })) },
            AnswerItem(stringResource(Res.string.quest_wheelchairAccess_limited)) { on(Answer(LIMITED.apply { updatedDescriptions = descriptions })) },
            AnswerItem(stringResource(Res.string.quest_generic_hasFeature_yes)) { on(Answer(YES.apply { updatedDescriptions = descriptions })) },
        ),
        otherAnswers = { listOf(
            AnswerItem(stringResource(Res.string.quest_wheelchair_description_answer)) { showDescriptionDialog = true }
        ) }
    )
    if (showDescriptionDialog) {
        val descriptionFieldValues = remember { mutableMapOf(*descriptions.map { it.key to TextFieldValue(it.value) }.toTypedArray()) }
        ScrollableAlertDialog(
            onDismissRequest = { showDescriptionDialog = false },
            buttonRow = {
                TextButton({ showDescriptionDialog = false }) { Text(stringResource(Res.string.cancel)) }
                TextButton({
                    descriptions = mapOf(*descriptionFieldValues.map { it.key to it.value.text }.toTypedArray())
                    showDescriptionDialog = false
                }) { Text(stringResource(Res.string.ok)) }
            },
            content = {
                descriptions.forEach { (language, text) ->
                    val hint = language.substringAfter(':').ifEmpty { stringResource(Res.string.quest_wheelchair_description_no_language) }
                    var value by remember { mutableStateOf(TextFieldValue(text)) }
                    TextField2(
                        value = value,
                        onValueChange = {
                            descriptionFieldValues[language] = it
                            value = it
                        },
                        placeholder = { Text(hint) },
                    )
                }
            }
        )
    }
}
