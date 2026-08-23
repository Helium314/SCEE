package de.westnordost.streetcomplete.quests.tree

import android.content.Context
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.preferences.addLastPicked
import de.westnordost.streetcomplete.data.preferences.getLastPicked
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_leafType_tree_is_just_a_stump
import de.westnordost.streetcomplete.screens.main.map.getTreeGenus
import de.westnordost.streetcomplete.ui.common.auto_complete_text.AutoCompleteTextField
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.LocalLastMapClick
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.theme.largeInput
import de.westnordost.streetcomplete.util.math.distanceTo
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.takeFavorites
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import java.io.File
import java.io.IOException
import java.text.Normalizer
import java.util.Locale
import java.util.regex.Pattern

@Composable
fun TreeGenusForm(on: (QuestAction<TreeAnswer>) -> Unit) {
    if (treeSet.isEmpty()) loadTrees(LocalContext.current)
    var name by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }
    var selectedTree by remember { mutableStateOf<Tree?>(null) }
    val prefs: Preferences = koinInject()
    val lastPicked = remember { prefs.getLastPicked<String>("AddTreeGenusForm").takeFavorites(20, 50, 1) }
    LaunchedEffect(name) { selectedTree = getSelectedTree(name.text.trim(), lastPicked) }

    val mapDataSource: MapDataWithEditsSource = koinInject()
    val mapClick = LocalLastMapClick.current
    LaunchedEffect(mapClick) { // todo: this doesn't work, also for road name form -> maybe SC issue?
        if (mapClick == null) return@LaunchedEffect
        val position = mapClick.position
        val maxDist = mapClick.clickAreaSizeInMeters + 5
        val data = mapDataSource.getMapDataWithGeometry(position.enclosingBoundingBox(maxDist))
        var bestTree: Pair<String, Double>? = null

        data.forEach { element ->
            if (element is Node && element.tags["natural"] == "tree") {
                val name = getTreeGenus(element.tags) ?: return@forEach
                val distance = element.position.distanceTo(position)
                if (distance < (bestTree?.second ?: maxDist))
                    bestTree = Pair(name, distance)
            }
        }
        val bestName = bestTree?.let { getTrees(it.first, lastPicked).firstOrNull()?.toDisplayString() }
        if (bestName != null) name = TextFieldValue(bestName)
    }

    QuestForm(
        on,
        isComplete = selectedTree != null,
        onClickOk = {
            val tree = selectedTree!!
            prefs.addLastPicked("AddTreeGenusForm", "${tree.isSpecies}§${tree.name}")
            on(Answer(tree))
        },
        otherAnswers = { listOf(AnswerItem(stringResource(Res.string.quest_leafType_tree_is_just_a_stump)) {
            on(Answer(NotTreeButStump))})
        }
    ) {
        AutoCompleteTextField(
            value = name,
            onValueChange = { name = it },
            suggestions = getTrees(name.text.trim(), lastPicked).map { it.toDisplayString() },
            textStyle = MaterialTheme.typography.largeInput,
        )
    }
}

private val treeSet = mutableSetOf<Tree>()

private fun loadTrees(context: Context) {
    if (treeSet.isNotEmpty()) return
    // load from file, assuming format: <Genus/Species> (<localName>)
    //  assume species if it contains a space character
    try {
        context.getExternalFilesDir(null)?.let { dir ->
            treeSet.addAll(File(dir, FILENAME_TREES).readLines().mapNotNull { it.toTree(it.substringBefore(" (").contains(" ")) })
        }
    } catch (_: IOException) { } // file may not exist, so an exception is no surprise

    try {
        context.assets.open("tree/otherDataGenus.txt").bufferedReader().lineSequence().mapNotNullTo(treeSet) { it.toTree(false) }
        context.assets.open("tree/otherDataSpecies.txt").bufferedReader().lineSequence().mapNotNullTo(treeSet) { it.toTree(true) }
        context.assets.open("tree/osmGenus.txt").bufferedReader().lineSequence().mapNotNullTo(treeSet) { it.toTree(false) }
        context.assets.open("tree/osmSpecies.txt").bufferedReader().lineSequence().mapNotNullTo(treeSet) { it.toTree(true) }
    } catch (_: IOException) { }
}

private fun String.toTree(isSpecies: Boolean): Tree? {
    val line = trim()
    if (line.isBlank()) return null
    val localName = if (line.contains(" (") && line.contains(')'))
        line.substringAfter("(").substringBeforeLast(")")
    else null
    return Tree(line.substringBefore(" (").intern(), isSpecies, localName)
}

private fun getTrees(fullSearch: String, lastPickedAnswers: List<String>): List<Tree> {
    val search = fullSearch.trim()
    // not working, i need a tree with the same name and species, but local name?
    if (search.isEmpty()) return lastPickedAnswers.mapNotNull { answer ->
        val treeString = answer.split('§')
        treeSet.firstOrNull { it.name == treeString[1] && it.isSpecies == (treeString[0] == "true") }
    }
    return treeSet.filter { tree ->
        tree.toDisplayString() == search
            || tree.toDisplayString().startsWith(search, true)
            || tree.name == search
            || tree.name.split(" ").any { it.startsWith(search, true) }
            || tree.localName?.contains(search, true) == true
        //sorting: genus-only first, then prefer trees with localName
    }.sortedBy { it.localName == null }.sortedBy { it.isSpecies }
}

const val FILENAME_TREES = "trees.csv"

private fun getSelectedTree(input: String, lastPickedAnswers: List<String>): Tree? {
    return getTrees(input, lastPickedAnswers).firstOrNull { canonicalize(it.toDisplayString()) == canonicalize(input) }
}

private val FIND_DIACRITICS: Pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")

private fun canonicalize(str: String): String {
    return stripDiacritics(str).lowercase(Locale.US)
}

private fun stripDiacritics(str: String): String {
    return FIND_DIACRITICS.matcher(Normalizer.normalize(str, Normalizer.Form.NFD)).replaceAll("")
}
