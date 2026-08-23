package de.westnordost.streetcomplete.quests

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.difflib.text.DiffRow.Tag
import com.github.difflib.text.DiffRowGenerator
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.ParseException
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_settings_element_selection
import de.westnordost.streetcomplete.ui.common.dialogs.InfoDialog
import de.westnordost.streetcomplete.ui.common.dialogs.ScrollableAlertDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import java.util.regex.PatternSyntaxException

// restarts are typically necessary on changes of element selection because the filter is created by lazy
// quests settings should follow the pattern: qs_<quest_name>_<something>, e.g. "qs_AddLevel_more_levels"
// when to call reloadQuestTypes: if whatever is changed is not read from settings every time, or if dynamic quest creation is enabled

fun OsmElementQuestType<*>.getPrefixedFullElementSelectionPref(prefs: Preferences) = "${questPrefix(prefs)}qs_${name}_full_element_selection"

fun questPrefix(prefs: Preferences) = if (prefs.getBoolean(Prefs.QUEST_SETTINGS_PER_PRESET, false))
    prefs.getLong(Preferences.SELECTED_EDIT_TYPE_PRESET, 0).toString() + "_"
else
    ""

fun getLabelSources(defaultValue: String, questType: OsmFilterQuestType<*>, prefs: Preferences) =
    prefs.getString(getPrefixedLabelSourcePref(questType, prefs),
        defaultValue
    ).split(",").map { it.trim() }

fun getPrefixedLabelSourcePref(questType: OsmElementQuestType<*>, prefs: Preferences) = "${questPrefix(prefs)}qs_${questType.name}_label_sources"

@Composable fun LabelOrElementSelectionDialog(questType: OsmFilterQuestType<*>, prefs: Preferences, onDismissRequest: () -> Unit) {
    val prefWithPrefix = getPrefixedLabelSourcePref(questType, prefs)
    var text by remember {
        mutableStateOf(TextFieldValue(prefs.getString(prefWithPrefix, questType.dotLabelSources.joinToString(", "))))
    }
    var showElementSelection by remember { mutableStateOf(false) }
    ScrollableAlertDialog(
        onDismissRequest = onDismissRequest,
        content = {
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                Text(
                    text = AnnotatedString.fromHtml(stringResource(R.string.quest_settings_dot_labels_message)),
                    style = MaterialTheme.typography.body1
                )
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false
                )
                Button({
                    showElementSelection = true
                }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.element_selection_button))
                }
            }
        },
        buttonRow = {
            ResetCancelOk(
                onDismissRequest = onDismissRequest,
                resetEnabled = prefs.contains(prefWithPrefix),
                onReset = { prefs.remove(prefWithPrefix); OsmQuestController.reloadQuestTypes() },
                okEnabled = true,
                onOk = {
                    prefs.putString(prefWithPrefix, text.text)
                    OsmQuestController.reloadQuestTypes()
                }
            )
        },
    )
    if (showElementSelection)
        FullElementSelectionDialog(
            prefs,
            questType.getPrefixedFullElementSelectionPref(prefs),
            Res.string.quest_settings_element_selection,
            questType.elementFilter
        ) { showElementSelection = false }
}

/** for setting values of a single positive number */
@Composable fun NumberSelectionDialog(prefs: Preferences, pref: String, defaultValue: Int, messageId: StringResource, onDismissRequest: () -> Unit) {
    var text by remember {
        mutableStateOf(TextFieldValue(prefs.getInt(pref, defaultValue).toString()))
    }
    var isOk by remember { mutableStateOf(true) }
    ScrollableAlertDialog(
        onDismissRequest = onDismissRequest,
        content = {
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                Text(
                    text = AnnotatedString.fromHtml(stringResource(messageId)),
                    style = MaterialTheme.typography.body1
                )
                TextField(
                    value = text,
                    onValueChange = {
                        isOk = it.text.toIntOrNull()?.let { it > 0 } == true
                        text = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        buttonRow = {
            ResetCancelOk(
                onDismissRequest = onDismissRequest,
                resetEnabled = prefs.contains(pref),
                onReset = { prefs.remove(pref); OsmQuestController.reloadQuestTypes() },
                okEnabled = isOk,
                onOk = {
                    val number = text.text.toIntOrNull() ?: return@ResetCancelOk
                    if (number == prefs.getInt(pref, defaultValue)) return@ResetCancelOk
                    prefs.putInt(pref, number)
                    if (prefs.getBoolean(Prefs.DYNAMIC_QUEST_CREATION, false))
                        OsmQuestController.reloadQuestTypes()
                }
            )
        },
    )
}

/** for setting values of a single key, comma separated */
@Composable fun SingleTypeElementSelectionDialog(
    prefs: Preferences,
    pref: String,
    defaultValue: String,
    messageId: StringResource,
    onDismissRequest: () -> Unit,
    onChanged: () -> Unit = { OsmQuestController.reloadQuestTypes() },
) {
    var text by remember {
        mutableStateOf(TextFieldValue(prefs.getString(pref, defaultValue).replace("|",", ")))
    }
    var isOk by remember { mutableStateOf(true) }
    ScrollableAlertDialog(
        onDismissRequest = onDismissRequest,
        content = {
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                Text(
                    text = AnnotatedString.fromHtml(stringResource(messageId)),
                    style = MaterialTheme.typography.body1
                )
                TextField(
                    value = text,
                    onValueChange = {
                        isOk = checkValueText(it.text)
                        text = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false
                )
            }
        },
        buttonRow = {
            ResetCancelOk(
                onDismissRequest = onDismissRequest,
                resetEnabled = prefs.contains(pref),
                onReset = { prefs.remove(pref); onChanged() },
                okEnabled = isOk,
                onOk = {
                    val prefText = text.text.split(",").joinToString("|") { it.trim() }
                    if (prefs.getString(pref, defaultValue) == prefText) return@ResetCancelOk
                    prefs.putString(pref, prefText)
                    onChanged()
                }
            )
        },
    )
}

/**
 *  For setting full element selection.
 *  This will check validity of input and only allow saving selection can be parsed.
 */
@Composable
fun FullElementSelectionDialog(prefs: Preferences, pref: String, messageId: StringResource, defaultValue: String, onDismissRequest: () -> Unit) {
    val checkPrefix = if (pref.endsWith("_full_element_selection")) "" else "nodes with "
    var text by remember {
        mutableStateOf(TextFieldValue(prefs.getString(pref, defaultValue.trimIndent())))
    }
    var isOk by remember { mutableStateOf(true) }
    val ctx = LocalContext.current
    ScrollableAlertDialog(
        onDismissRequest = onDismissRequest,
        content = {
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                Text(
                    text = AnnotatedString.fromHtml(org.jetbrains.compose.resources.stringResource(messageId)),
                    style = MaterialTheme.typography.body1
                )
                TextField(
                    value = text,
                    onValueChange = {
                        isOk = checkText(it.text, checkPrefix, ctx)
                        text = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false
                )
                if (prefs.contains(pref))
                    DiffButton(defaultValue) { text.text }
            }
        },
        buttonRow = {
            ResetCancelOk(
                onDismissRequest = onDismissRequest,
                resetEnabled = prefs.contains(pref),
                onReset = { prefs.remove(pref); OsmQuestController.reloadQuestTypes() },
                okEnabled = isOk,
                onOk = {
                    if (text.text != prefs.getString(pref, defaultValue.trimIndent())) {
                        prefs.putString(pref, text.text)
                        OsmQuestController.reloadQuestTypes()
                    }
                }
            )
        },
    )
}

@Composable fun FlowRowScope.ResetCancelOk(
    onDismissRequest: () -> Unit,
    resetEnabled: Boolean,
    onReset: () -> Unit,
    okEnabled: Boolean,
    onOk: () -> Unit
) {
    TextButton(
        onClick = { onReset(); onDismissRequest() },
        enabled = resetEnabled,
        modifier = Modifier.padding(end = 16.dp)
    ) {
        Text(stringResource(R.string.quest_settings_reset))
    }
    TextButton(onDismissRequest) { Text(stringResource(android.R.string.cancel)) }
    TextButton(
        onClick = { onOk(); onDismissRequest() },
        enabled = okEnabled
    ) {
        Text(stringResource(android.R.string.ok))
    }
}

@Composable
private fun DiffButton(defaultText: String, getCurrentText: () -> String) {
    var showDialog by remember { mutableStateOf(false) }
    Button({
        showDialog = true
    }, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.quest_settings_highlight_changes_button))
    }
    if (showDialog) {
        val drg = DiffRowGenerator.create()
            .showInlineDiffs(true)
            .mergeOriginalRevised(true)
            .inlineDiffByWord(true)
            .ignoreWhiteSpaces(true)
            .oldTag { f -> if (f) "<b><i><del>" else "</del></i></b>" }
            .newTag { f -> if (f) "<b><u><ins>" else "</ins></u></b>" }
            .build()
        val thatSpace = " "
        val newDefault = defaultText.replace("|", "$thatSpace|$thatSpace") // replace with (nearly) invisible space, so word differences are used
        val newCurrent = getCurrentText().replace("|", "$thatSpace|$thatSpace")
        val diffRows = drg.generateDiffRows(newDefault.split("\n"), newCurrent.split("\n"))
        val diffText = diffRows.mapNotNull {
            if (it.tag == Tag.EQUAL) return@mapNotNull null
            it.oldLine
        }.joinToString("<br>")
        InfoDialog(
            onDismissRequest = { showDialog = false },
            text = { Text(AnnotatedString.fromHtml(diffText), style = MaterialTheme.typography.body1) }
        )
    }
}

@Composable fun BooleanQuestSettingsDialog(
    prefs: Preferences,
    key: String,
    default: Boolean,
    messageId: StringResource,
    answerTrue: StringResource,
    answerFalse: StringResource,
    onDismissRequest: () -> Unit
) {
    InfoDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(messageId)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    {
                        if (default) prefs.remove(key) else prefs.putBoolean(key, true)
                        onDismissRequest()
                    },
                    Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(answerTrue), textAlign = TextAlign.Center)
                }
                Button(
                    {
                        if (!default) prefs.remove(key) else prefs.putBoolean(key, false)
                        onDismissRequest()
                    },
                    Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(answerFalse), textAlign = TextAlign.Center)
                }
            }
        }
    )
}

private val valueRegex = "[a-z\\d_?,/\\s]+".toRegex()
private fun checkValueText(text: String) =
    text.lowercase().matches(valueRegex)
        && !text.trim().endsWith(',')
        && !text.contains(",,")
        && text.isNotEmpty()

// relax a little bit? but e.g. A-Z is very uncommon and might lead to mistakes
private val elementSelectionRegex = "[a-z\\d_=!?\"~*\\[\\]()|:.,<>\\s+-]+".toRegex()
// toasting is not Multiplatform...
private fun checkText(text: String, checkPrefix: String, context: Context): Boolean {
    val isValidFilterExpression by lazy {
        try {
            (checkPrefix + text).toElementFilterExpression()
            toastyJob?.cancel()
            true
        } catch(e: ParseException) {
            delayedToast(e.message, context)
            false
        } catch(e: PatternSyntaxException) {
            delayedToast(e.message, context)
            false
        }
    }
    // check other stuff first, because creation filter expression is relatively slow
    return (checkPrefix.isEmpty() || text.lowercase().matches(elementSelectionRegex))
        && text.count { c -> c == '('} == text.count { c -> c == ')'}
        && (text.contains('=') || text.contains('~') || text.contains('!'))
        && isValidFilterExpression
}

private var toastyJob: Job? = null
private fun delayedToast(message: String?, context: Context) {
    toastyJob?.cancel()
    toastyJob = GlobalScope.launch(Dispatchers.IO) {
        delay(3000)
        withContext(Dispatchers.Main) { Toast.makeText(context, "Error: $message", Toast.LENGTH_LONG).show() }
    }
}
