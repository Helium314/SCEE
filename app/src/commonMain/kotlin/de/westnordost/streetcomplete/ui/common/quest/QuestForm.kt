package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.osmquests.Action
import de.westnordost.streetcomplete.data.osm.osmquests.Action.*
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.accessKeys
import de.westnordost.streetcomplete.osm.places.isPlaceOrDisusedPlace
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.FloatingOkButton
import de.westnordost.streetcomplete.ui.common.FloatingSmallerButton
import de.westnordost.streetcomplete.ui.common.bottom_sheet.BottomSheetFormScaffold
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmDiscardDialog
import de.westnordost.streetcomplete.ui.theme.defaultTextLinkStyles
import de.westnordost.streetcomplete.ui.theme.titleSmall
import de.westnordost.streetcomplete.ui.util.annotateLinks
import de.westnordost.streetcomplete.util.ktx.containsAnyKey
import de.westnordost.streetcomplete.util.ktx.isDeletable
import de.westnordost.streetcomplete.util.ktx.isSplittable
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** A generic quest form, with a [title], [subtitle], [hintText] and [hintImages] in the
 *  header speech bubble, then an optional [note] by another mapper shown below as another speech
 *  bubble, then finally the speech bubble containing the center-aligned [content] padded with a
 *  [contentPadding] (if there is any content) and an OK button to confirm the input.
 *
 *  **This composable requires the `LocalQuestType` composition local to be set!**
 *
 *  At the very start of the text button row, there's a text button labeled "Uh…" that, when tapped,
 *  opens a dropdown menu containing [otherAnswers] (defined from start to bottom). */
@Composable
fun QuestForm(
    on: (Action) -> Unit,
    isComplete: Boolean,
    onClickOk: () -> Unit,
    modifier: Modifier = Modifier,
    featureDictionary: FeatureDictionary = koinInject(),
    hasChanges: Boolean = isComplete,
    title: String = stringResource(LocalQuestType.current!!.title),
    subtitle: AnnotatedString? = LocalElement.current?.let { element ->
        nameAndLocationLabel(element, featureDictionary)
    },
    hintText: String? = LocalQuestType.current!!.hint?.let { stringResource(it) },
    hintImages: List<DrawableResource> = LocalQuestType.current!!.hintImages,
    note: String? = LocalElement.current?.tags?.get("note"),
    otherAnswers: @Composable () -> List<AnswerItem> = { emptyList() },
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
    content: @Composable BoxScope.() -> Unit
) {
    QuestForm(
        on = on,
        title = title,
        subtitle = subtitle,
        hintText = hintText,
        hintImages = hintImages,
        note = note,
        isComplete = isComplete,
        hasChanges = hasChanges,
        onClickOk = onClickOk,
        answers = emptyList(),
        otherAnswers = otherAnswers,
        contentPadding = contentPadding,
        modifier = modifier,
        content = content,
    )
}

/** A generic quest form, with a [title], [subtitle], [hintText] and [hintImages] in the
 *  header speech bubble, then an optional [note] by another mapper shown below as another speech
 *  bubble, then finally the speech bubble containing the center-aligned [content] padded with a
 *  [contentPadding] (if there is any content) and below a row of text buttons showing
 *  different [answers] (defined from start to end).
 *
 *  **This composable requires the `LocalQuestType` composition local to be set!**
 *
 *  At the very start of the text button row, there's a text button labeled "Uh…" that, when tapped,
 *  opens a dropdown menu containing [otherAnswers] (defined from start to bottom). */
@Composable
fun QuestForm(
    on: (Action) -> Unit,
    answers: List<AnswerItem>,
    modifier: Modifier = Modifier,
    featureDictionary: FeatureDictionary = koinInject(),
    title: String = stringResource(LocalQuestType.current!!.title),
    subtitle: AnnotatedString? = LocalElement.current?.let { element ->
        nameAndLocationLabel(element, featureDictionary)
    },
    hintText: String? = LocalQuestType.current!!.hint?.let { stringResource(it) },
    hintImages: List<DrawableResource> = LocalQuestType.current!!.hintImages,
    note: String? = LocalElement.current?.tags?.get("note"),
    otherAnswers: @Composable () -> List<AnswerItem> = { emptyList() },
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
    content: @Composable (BoxScope.() -> Unit)? = null
) {
    QuestForm(
        on = on,
        title = title,
        subtitle = subtitle,
        hintText = hintText,
        hintImages = hintImages,
        note = note,
        isComplete = true,
        hasChanges = false,
        onClickOk = null,
        answers = answers,
        otherAnswers = otherAnswers,
        contentPadding = contentPadding,
        modifier = modifier,
        content = content,
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun QuestForm(
    on: (Action) -> Unit,
    title: String,
    subtitle: AnnotatedString?,
    hintText: String?,
    hintImages: List<DrawableResource>,
    note: String?,
    isComplete: Boolean,
    hasChanges: Boolean,
    onClickOk: (() -> Unit)?,
    answers: List<AnswerItem>,
    otherAnswers: @Composable () -> List<AnswerItem>,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    mapDataWithEditsSource: MapDataWithEditsSource = koinInject(),
    content: @Composable (BoxScope.() -> Unit)?,
) {
    val prefs: Preferences = koinInject()
    val element = LocalElement.current
    val fixme = element?.tags?.get("fixme") ?: element?.tags?.get("FIXME")

    var confirmDiscard by remember { mutableStateOf(false) }

    BackHandler {
        if (hasChanges) {
            confirmDiscard = true
        } else {
            on(Action.Dismiss)
        }
    }

    @Composable
    fun createDefaultOtherAnswers(): List<AnswerItem> {
        if (LocalIsTagEditor.current) return emptyList()
        val result = ArrayList<AnswerItem>()
        if (
            element is Node // add moveNodeAnswer only if it's a free floating node or expert mode
            && (prefs.expertMode || mapDataWithEditsSource.getWaysForNode(element.id).isEmpty())
        ) {
            result.add(AnswerItem(stringResource(Res.string.move_node)) { on(MoveNode) })
        }
        if (element?.isPlaceOrDisusedPlace() == true) {
            result.add(AnswerItem(stringResource(Res.string.quest_generic_answer_does_not_exist)) { on(ReplacePoi) })
        }
        if (element?.isDeletable() == true) {
            result.add(AnswerItem(stringResource(Res.string.quest_generic_answer_does_not_exist)) { on(DeletePoi) })
        }
        if (element?.isSplittable() == true) {
            result.add(AnswerItem(stringResource(Res.string.quest_generic_answer_differs_along_the_way)) { on(SplitWay) })
        }
        if (element != null && prefs.expertMode) {
            result.add(AnswerItem(stringResource(Res.string.quest_generic_answer_show_edit_tags)) { on(EditTags) })
        }
        if (element != null && prefs.expertMode && pathsAndRoadsFilter.matches(element)) {
            val res = if (element.tags.containsAnyKey(*accessKeys.toTypedArray())) Res.string.manage_access
                else Res.string.add_access
            result.add(AnswerItem(stringResource(res)) { on(ManageAccess) })
        }
        if (element != null && prefs.expertMode && elementWithoutAccessTagsFilter.matches(element)
                && pathsAndRoadsFilter.matches(element)) {
            result.add(AnswerItem(stringResource(Res.string.quest_construction)) { on(UnderConstruction) })
        }
        result.add(AnswerItem(stringResource(Res.string.quest_generic_answer_notApplicable)) { on(CantSay) })
        return result
    }

    BottomSheetFormScaffold(
        header = {
            QuestHeader(
                title = title,
                subtitle = subtitle,
                hintText = hintText,
                hintImages = hintImages,
            )
        },
        note = if (note != null) { {
            ObjectNote(text = note)
        } } else null,
        fixme = if (fixme != null) { {
            ObjectFixme(text = fixme)
        } } else null,
        content = {
            QuestAnswerContent(
                modifier = Modifier.fillMaxWidth(),
                answers = answers,
                otherAnswers = { otherAnswers() + createDefaultOtherAnswers() },
                contentPadding = contentPadding,
                content = content,
            )
        },
        fab = if (onClickOk != null) {
            { FloatingOkButton(visible = isComplete, onClick = onClickOk) }
        } else null,
        fab2 = {
            HideButton({ on(Action.HideQuest) }, { on(Action.TempHideQuest) })
        },
        modifier = modifier,
    )

    if (confirmDiscard) {
        ConfirmDiscardDialog(
            onDismissRequest = { confirmDiscard = false },
            onConfirmed = { on(Action.Dismiss) },
        )
    }
}

/** Speech bubble (without arrow) that contains a note another user left for this object */
@Composable
private fun ObjectNote(
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            text = stringResource(Res.string.note_for_object),
            style = MaterialTheme.typography.titleSmall
        )
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            SelectionContainer {
                Text(
                    text = text.annotateLinks(MaterialTheme.typography.defaultTextLinkStyles()),
                    style = MaterialTheme.typography.body2,
                )
            }
        }
    }
}

@Composable
private fun ObjectFixme(
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            text = stringResource(Res.string.fixme_for_object),
            style = MaterialTheme.typography.titleSmall
        )
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            SelectionContainer {
                Text(
                    text = text.annotateLinks(MaterialTheme.typography.defaultTextLinkStyles()),
                    style = MaterialTheme.typography.body2,
                )
            }
        }
    }
}

@Composable
fun HideButton(onHide: () -> Unit, onTempHide: () -> Unit) {
    val prefs: Preferences = koinInject()
    if (prefs.getBoolean(Prefs.SHOW_HIDE_BUTTON, false))
        FloatingSmallerButton(
            onClick = onTempHide,
            modifier = Modifier.combinedClickable(onLongClick = onHide, onClick = onTempHide)
        ) {
            Text(stringResource(Res.string.hide_button))
        }
}

val pathsAndRoadsFilter = "ways with highway ~ ${(ALL_ROADS + ALL_PATHS).joinToString("|")}"
    .toElementFilterExpression()

// check the most common access tags
val elementWithoutAccessTagsFilter = """
nodes, ways, relations with
 !access
 and !access:conditional
 and !bicycle
 and !bicycle:conditional
 and !foot
 and !foot:conditional
 and !vehicle
 and !vehicle:conditional
 and !motor_vehicle
 and !motor_vehicle:conditional
 and !motorcycle
 and !motorcycle:conditional
 and !horse
 and !bus
 and !hgv
 and !motorcar
 and !psv
 and !ski
    """.toElementFilterExpression()
