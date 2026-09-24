package de.westnordost.streetcomplete.osm

import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChanges
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_construction_completion_date_title
import de.westnordost.streetcomplete.resources.quest_construction_hint
import de.westnordost.streetcomplete.resources.quest_construction_unknown_end
import de.westnordost.streetcomplete.resources.quest_construction_value
import de.westnordost.streetcomplete.ui.common.DateSelectDialog
import de.westnordost.streetcomplete.ui.common.dialogs.TextInputDialog
import de.westnordost.streetcomplete.util.ktx.systemTimeNow
import de.westnordost.streetcomplete.util.ktx.toLocalDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import org.jetbrains.compose.resources.stringResource
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ConstructionDialog(
    onDismissRequest: () -> Unit,
    element: Element,
    onEdit: (StringMapChanges) -> Unit
) {
    val tomorrow = remember { systemTimeNow().toLocalDate().plus(1, DateTimeUnit.DAY) }
    var constructionValueAndFinishDate by remember { mutableStateOf<Pair<String, LocalDate?>?>(null) }

    fun proceedToConstructionValue(openingDate: LocalDate?) {
        constructionValueAndFinishDate = element.tags["highway"]!! to openingDate
    }

    DateSelectDialog(
        onDismissRequest = onDismissRequest,
        onSelect = { finishDate ->
            val today = systemTimeNow().toLocalDate()
            val diff = finishDate.toEpochDays() - today.toEpochDays()
            if (diff < 0) return@DateSelectDialog

            val builder = StringMapChangesBuilder(element.tags)
            // for short construction up to a few months it's better to use conditional access
            // as per https://wiki.openstreetmap.org/wiki/Tag:highway%3Dconstruction
            if (diff < 200) { // we arbitrarily set the few months to 200 days
                val f = DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.US)
                builder["access:conditional"] = "no @ (${f.format(today.toJavaLocalDate())}-${f.format(finishDate.toJavaLocalDate())})"
                onEdit(builder.create())
            } else {
                // if we actually change the highway to construction, we let the user set a construction value
                proceedToConstructionValue(finishDate)
            }
        },
        initialDate = tomorrow,
        years = tomorrow.year..(tomorrow.year + 30),
        title = { Text(stringResource(Res.string.quest_construction_completion_date_title)) },
        dismissOnSelect = false,
        text = { Text(stringResource(Res.string.quest_construction_hint)) },
        neutralButton = {
            TextButton(onClick = { proceedToConstructionValue(null) }) {
                Text(stringResource(Res.string.quest_construction_unknown_end))
            }
        }
    )

    if (constructionValueAndFinishDate != null) {
        val (previousHighway, openingDate) = constructionValueAndFinishDate!!
        TextInputDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(stringResource(Res.string.quest_construction_value)) },
            text = previousHighway,
            onConfirmed = {
                val builder = StringMapChangesBuilder(element.tags)
                if (openingDate != null) {
                    val f = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
                    builder["opening_date"] = f.format(openingDate.toJavaLocalDate())
                }
                builder["highway"] = "construction"
                builder["construction"] = it
                onEdit(builder.create())
            }
        )
    }
}
