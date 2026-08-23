package de.westnordost.streetcomplete.screens.main.bottom_sheet

import android.content.res.Resources
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChanges
import de.westnordost.streetcomplete.data.osm.edits.update_tags.createChanges
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_add_24
import de.westnordost.streetcomplete.resources.ic_undo_24
import de.westnordost.streetcomplete.resources.tag_editor_last_edited
import de.westnordost.streetcomplete.ui.common.Button2
import de.westnordost.streetcomplete.ui.common.FloatingOkButton
import de.westnordost.streetcomplete.ui.common.auto_complete_text.AutoCompleteTextField
import de.westnordost.streetcomplete.ui.common.bottom_sheet.BottomSheetFormScaffold
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmDiscardDialog
import de.westnordost.streetcomplete.ui.common.opening_hours.DeleteRowButton
import de.westnordost.streetcomplete.ui.common.quest.LocalElement
import de.westnordost.streetcomplete.util.locale.getLanguagesForFeatureDictionary
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditTagsForm(
    onConfirmed: (StringMapChanges) -> Unit,
    onDismiss: () -> Unit,
    originalElement: Element = LocalElement.current!!
) {
    val mapDataSource: MapDataWithEditsSource = koinInject()
    val featureDictionary: FeatureDictionary = koinInject()
    val prefs: Preferences = koinInject()
    val osmQuestController: OsmQuestController = koinInject()
    val resources = LocalResources.current
    var updatedTags by rememberSaveable { mutableStateOf<Map<String, String>>(originalElement.tags.toSortedMap()) }
    var confirmDiscard by remember { mutableStateOf(false) }
    BackHandler {
        if (updatedTags != originalElement.tags) {
            confirmDiscard = true
        } else {
            onDismiss()
        }
    }
    val lastFeature = remember { featureDictionary.byTags(updatedTags).isSuggestion(false).find().firstOrNull() }
    val keySuggestions = remember { getKeySuggestions(lastFeature?.id, updatedTags, prefs, resources).toList() }

    BottomSheetFormScaffold(
        content = {
            ProvideTextStyle(MaterialTheme.typography.body1) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { // lazy column has infinite height, so we need a normal column
                    updatedTags.forEach { (k, v) ->
                        // todo: width is weird, why is 0.4 and 0.8 necessary to have almost equal widths? this has weird effect on AutoCompleteTextField dropdown
                        // todo: suggestions in AutoCompleteTextField should not show if empty after filtering or the only suggestion is already written
                        // todo: option for AutoCompleteTextField to start expanded
                        // todo: control focus (selecting a key or return moves to value, adding a new row focuses on key)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            var key by remember { mutableStateOf(TextFieldValue(k)) }
                            var value by remember { mutableStateOf(TextFieldValue(v)) }
                            // deleting has some issues, try updating here
                            if (key.text != k)
                                key = TextFieldValue(k)
                            if (value.text != v)
                                value = TextFieldValue(v)

                            var valueSuggestions by remember { mutableStateOf(listOf<String>()) }
                            LaunchedEffect(key) {
                                val suggestions = mutableListOf<String>()
                                prefs.getString("EditTagsAdapter_${key.text}_values", "")
                                    .split("§§").forEach {
                                        if (it.startsWith(value.text) && it.isNotEmpty())
                                            suggestions.add(it)
                                    }
                                suggestions.addAll(valueSuggestionsByKey[key.text].orEmpty())
                                valueSuggestions = suggestions
                            }

                            AutoCompleteTextField(
                                value = key,
                                onValueChange = {
                                    // use entries here, because we want to preserve the order in the map
                                    val entries = updatedTags.entries.mapTo(mutableListOf()) { it.key to it.value }
                                    val i = entries.indexOfFirst { it.first == key.text }
                                    entries[i] = it.text to entries[i].second
                                    key = it
                                    updatedTags = mapOf(*entries.toTypedArray())
                                },
                                modifier = Modifier.fillMaxWidth(0.4f),
                                suggestions = keySuggestions
                            )
                            Spacer(Modifier.size(6.dp))
                            AutoCompleteTextField(
                                value = value,
                                onValueChange = {
                                    val m = updatedTags.toMutableMap()
                                    m[key.text] = it.text
                                    value = it
                                    updatedTags = m
                                },
                                modifier = Modifier.fillMaxWidth(0.8f),
                                suggestions = valueSuggestions
                            )
                            DeleteRowButton(
                                onClick = {
                                    if (value.text.isNotEmpty()) {
                                        value = TextFieldValue()
                                        updatedTags += key.text to ""
                                    }
                                    else updatedTags -= key.text
                                }
                            )
                        }
                    }
                    val date = Date(originalElement.timestampEdited)
                    val dateText = stringResource(Res.string.tag_editor_last_edited, DateFormat.getDateTimeInstance().format(date))
                    val url = "https://www.openstreetmap.org/${originalElement.type.name.lowercase()}/${originalElement.id}/history"
                    val text = buildAnnotatedString {
                        withLink(LinkAnnotation.Url(url)) {
                            append(dateText)
                        }
                    }
                    Text(text)
                    FlowRow(itemVerticalAlignment = Alignment.CenterVertically) {
                        Button2({ updatedTags += emptyEntry }) { Icon(painterResource(Res.drawable.ic_add_24), "add tag") }
                        if (originalElement.id == 0L) {
                            val previousTagsForFeature: Map<String, String>? = try { featureDictionary
                                .getByTags(
                                    tags = originalElement.tags,
                                    isSuggestion = false,
                                    languages = getLanguagesForFeatureDictionary()
                                ).firstOrNull()
                                ?.let { prefs.getString(Prefs.CREATE_NODE_LAST_TAGS_FOR_FEATURE + it, "") }
                                ?.let { Json.decodeFromString(it) }
                            } catch (e: Exception) { null }
                            if (previousTagsForFeature?.isNotEmpty() == true && previousTagsForFeature != originalElement.tags)
                                Button2({ updatedTags = previousTagsForFeature.toSortedMap() }) {
                                    Icon(painterResource(Res.drawable.ic_undo_24), "redo", modifier = Modifier.scale(-0.7f, 0.7f))
                                }
                        }
                        // todo: quests (if still possible)
                        //  we need the CompositionLocal stuff
                        //  and a way to actually access the action value
                        //  then just try to show the form instead of the column
                        // just showing the quest form should allow to avoid the default other answers
/*                        val element = originalElement.copy(tags = updatedTags, timestampEdited = nowAsEpochMilliseconds())
                        val geometry = mapDataSource.getGeometry(element.type, element.id) ?: ElementPointGeometry((originalElement as Node).position)
                        val quests = runBlocking { osmQuestController.createNonPoiQuestsForElement(element, geometry) }
                        val questType = quests.first().type
                        questType.Form(
                            { action ->
                                if (action is Answer) {
                                    val changesBuilder = StringMapChangesBuilder(element.tags)
                                    questType.applyAnswerTo(action.value, changesBuilder, geometry, element.timestampEdited)
                                    val changes = changesBuilder.create()
                                    onEdit(UpdateElementTagsAction(element, changes))
                                }
                            },
                            element,
                            geometry
                        )*/
                    }
                }
            }
        },
        fab = {
            FloatingOkButton(
                visible = tagsChangedAndOk(originalElement, updatedTags, mapDataSource),
                onClick = {
                    val newTags = updatedTags.mapNotNull {
                        (k, v) -> if (k.isBlank() && v.isBlank()) null else k.trim() to v.trim()
                    }.toMap()
                    if (lastFeature != null)
                        newTags.forEach { (k, v) ->
                            // store key for feature
                            if (!originalElement.tags.contains(k)) {
                                val keys = linkedSetOf(k)
                                val pref = "EditTagsAdapter_${lastFeature.id}_keys"
                                keys.addAll(prefs.getString(pref, "").split("§§"))
                                prefs.putString(pref, keys.take(15).joinToString("§§"))
                            }
                            // store value for key
                            if (originalElement.tags[k] != v) {
                                val values = linkedSetOf(v)
                                val pref = "EditTagsAdapter_${k}_values"
                                values.addAll(prefs.getString(pref, "").split("§§"))
                                prefs.putString(pref, values.take(15).joinToString("§§"))
                            }
                        }

                    onConfirmed(newTags.createChanges(originalElement.tags).create())
                },
            )
        },
    )
    if (confirmDiscard) {
        ConfirmDiscardDialog(
            onDismissRequest = { confirmDiscard = false },
            onConfirmed = onDismiss,
        )
    }
}

private val emptyEntry = "" to ""

private fun tagsChangedAndOk(originalElement: Element, newTags: Map<String, String>, mapDataSource: MapDataWithEditsSource): Boolean =
    newTags != mapOf(emptyEntry)
        && (originalElement.id == 0L || originalElement.tags != HashMap<String, String>().apply {
            newTags.forEach { (k, v) ->
                if (k.isBlank() || v.isBlank()) return@forEach
                put(k.trim(), v.trim())
            }
        })
        && newTags.none {
            (it.key.isBlank() && it.value.isNotBlank()) || (it.value.isBlank() && it.key.isNotBlank())
        }
        && newTags.keys.all { it.length < 255 }
        && newTags.values.all { it.length < 255 }
        && (newTags.isNotEmpty() || mapDataSource.getWaysForNode(originalElement.id).isNotEmpty()) // allow deleting all tags if node is part of a way
        && newTags.keys.none { problematicKeyCharacters.containsMatchIn(it.trim()) } // trim happens on ok, so no need to fail a check. see #822 / #824

// characters that should not be in keys, see https://taginfo.openstreetmap.org/reports/characters_in_keys
private val problematicKeyCharacters = "[\\s=+/&<>;'\"?%#@,\\\\]".toRegex()

private fun getKeySuggestions(featureId: String?, tags: Map<String, String>, prefs: Preferences, resources: Resources): Collection<String> {
    readSuggestions(resources)
    val suggestions = prefs.getString("EditTagsAdapter_${featureId}_keys", "").split("§§").filter { it.isNotEmpty() }.toMutableSet()
    if (featureId == null) return suggestions.filterNot { it in tags.keys }
    val fields = getMainSuggestions(featureId)
    val moreFields = getSecondarySuggestions(featureId)
    val fieldSuggestions = mutableListOf<String>()
    val moreFieldSuggestions = mutableListOf<String>()
    fields.forEach {
        if (it == "building" || it.startsWith("gnis:feature_id") ) return@forEach
        if (it.startsWith('{')) // does this actually trigger? or is it unnecessary?
            fieldSuggestions.addAll(getMainSuggestions(it.substringAfter('{').substringBefore('}')))
        else fieldSuggestions.add(it)
    }
    moreFields.forEach {
        // ignore some moreFields that often are inappropriate (but keep if in fields!)
        if (it.startsWith("ref:") || it.startsWith("building") || it == "gnis:feature_id" || it == "ele" || it == "height" ) return@forEach
        if (it.startsWith('{'))
            moreFieldSuggestions.addAll(getSecondarySuggestions(it.substringAfter('{').substringBefore('}')))
        else moreFieldSuggestions.add(it)
    }

    // suggestions should not be cluttered with all those address tags, but we don't want to ignore them completely
    // but we want to ignore some refs, and building which shows up for shops, but is usually not a good idea because we ignore geometry
    val fieldsMoveToEnd = fieldSuggestions.filter { it.startsWith("addr:") || it.startsWith("ref:") || it.startsWith("tiger:") }
    fieldSuggestions.removeAll(fieldsMoveToEnd)
    fieldSuggestions.removeAll { it.startsWith("{") } // appeared in the latest presets update, maybe we should actually go deeper for this '{'?
    val moreFieldsMoveToEnd = moreFieldSuggestions.filter { it.startsWith("addr:") || it.startsWith("ref:") || it.startsWith("tiger:") }
    moreFieldSuggestions.removeAll(moreFieldsMoveToEnd)
    moreFieldSuggestions.removeAll { it.startsWith("{") }

    // order: previously entered values, fields, moreFields, addr fields, addr moreFields
    // do it in this complicated way because we don't want to (re)move keys the user has entered
    suggestions.addAll(fieldSuggestions)
    suggestions.addAll(moreFieldSuggestions)
    suggestions.addAll(fieldsMoveToEnd)
    suggestions.addAll(moreFieldsMoveToEnd)

    suggestions.removeAll(tags.keys) // don't suggest what we already have
    return suggestions
    // can be optimized, but likely not worth the work
}

private fun getMainSuggestions(featureId: String): List<String> {
    val suggestions = keySuggestionsForFeatureId[featureId]?.first
    return suggestions ?: if (featureId.contains('/')) getMainSuggestions(featureId.substringBeforeLast('/')) else emptyList()
}

private fun getSecondarySuggestions(featureId: String): List<String> {
    val suggestions = keySuggestionsForFeatureId[featureId]?.second
    return suggestions ?: if (featureId.contains('/')) getSecondarySuggestions(featureId.substringBeforeLast('/')) else emptyList()
}

private val keySuggestionsForFeatureId = hashMapOf<String, Pair<List<String>?, List<String>?>>()
private val valueSuggestionsByKey = hashMapOf<String, List<String>>()

private fun readSuggestions(resources: Resources) {
    if (keySuggestionsForFeatureId.isEmpty() && valueSuggestionsByKey.isEmpty()) {
        try {
            val keySuggestions = resources.assets.open("tag_editor/keySuggestionsForFeature.json").reader().readText()
            val valueSuggestions = resources.assets.open("tag_editor/valueSuggestionsByKey.json").reader().readText()
            // filling maps twice is a bit inefficient, but there are so many duplicate strings that interning is worth it
            Json.decodeFromString<Map<String, Pair<List<String>?, List<String>?>>>(keySuggestions).forEach {
                keySuggestionsForFeatureId[it.key.intern()] = it.value.first?.map { it.intern() } to it.value.second?.map { it.intern() }
            }
            Json.decodeFromString<Map<String, List<String>>>(valueSuggestions).forEach {
                valueSuggestionsByKey[it.key.intern()] = it.value.map { it.intern() }
            }
        } catch (e: Exception) {
            Log.w("EditTagsForm", "failed to read and parse suggestions: ${e.message}")
        }
    }
}
