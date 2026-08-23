package de.westnordost.streetcomplete.quests.crossing_markings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.preferences.addLastPicked
import de.westnordost.streetcomplete.data.preferences.getLastPicked
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ItemsSelectGrid
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.util.takeFavorites
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import kotlin.collections.plus

// copy of ItemsSelectQuestForm where NO can't be selected at the same time as other answers
@Composable
fun CrossingMarkingsForm(
    on: (QuestAction<Set<CrossingMarkings>>) -> Unit,
    items: List<CrossingMarkings>,
    itemContent: @Composable (item: CrossingMarkings) -> Unit,
    modifier: Modifier = Modifier,
    itemsPerRow: Int = 3,
    favoriteKey: String? = null,
    otherAnswers: @Composable () -> List<AnswerItem> = { emptyList() },
    preferences: Preferences = koinInject()
) {
    val reorderedItems = remember(items, itemsPerRow, favoriteKey) {
        if (favoriteKey != null) {
            val favourites = preferences.getLastPicked<CrossingMarkings>(favoriteKey)
                .takeFavorites(n = itemsPerRow)
            (favourites + items).distinct()
        } else {
            items
        }
    }
    var selectedItems by rememberSerializable { mutableStateOf<Set<CrossingMarkings>>(emptySet()) }

    QuestForm(
        on = on,
        isComplete = selectedItems.isNotEmpty(),
        onClickOk = {
            if (favoriteKey != null) {
                preferences.addLastPicked(favoriteKey, selectedItems.toList())
            }
            on(Answer(selectedItems))
        },
        modifier = modifier,
        otherAnswers = otherAnswers,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CompositionLocalProvider(
                LocalContentAlpha provides ContentAlpha.medium,
                LocalTextStyle provides MaterialTheme.typography.body2
            ) {
                Text(stringResource(Res.string.quest_multiselect_hint))
            }
            ItemsSelectGrid(
                columns = SimpleGridCells.Fixed(itemsPerRow),
                items = reorderedItems,
                selectedItems = selectedItems,
                onSelect = { item, selected ->
                    if (selected) {
                        selectedItems += item
                    } else {
                        selectedItems -= item
                    }
                    if (item == CrossingMarkings.NO && selected)
                        selectedItems = setOf(CrossingMarkings.NO)
                    if (item != CrossingMarkings.NO && selected)
                        selectedItems -= CrossingMarkings.NO
                },
                modifier = Modifier.fillMaxWidth(),
                itemContent = itemContent
            )
        }
    }
}
