@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterialApi::class)

package de.westnordost.streetcomplete.quests.destination

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.preferences.addLastPicked
import de.westnordost.streetcomplete.data.preferences.getLastPicked
import de.westnordost.streetcomplete.osm.oneway.isOneway
import de.westnordost.streetcomplete.osm.oneway.isReversedOneway
import de.westnordost.streetcomplete.quests.lanes.Lanes
import de.westnordost.streetcomplete.quests.lanes.LanesSelect
import de.westnordost.streetcomplete.quests.lanes.LineStyle
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.auto_complete_text.AutoCompleteTextField
import de.westnordost.streetcomplete.ui.common.quest.LocalMapRotation
import de.westnordost.streetcomplete.ui.common.quest.LocalMapTilt
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.common.street_side_select.MiniCompass
import de.westnordost.streetcomplete.ui.theme.largeInput
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.math.enlargedBy
import de.westnordost.streetcomplete.util.math.getOrientationOrZero
import de.westnordost.streetcomplete.util.takeFavorites
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

private const val LAST_PICKED_KEY = "AddDestinationForm"

@Composable
fun AddDestinationForm(
    on: (QuestAction<Pair<DestinationLanes?, DestinationLanes?>>) -> Unit,
    element: Element,
    geometry: ElementGeometry,
    countryInfo: CountryInfo,
    mapDataSource: MapDataWithEditsSource = koinInject(),
    prefs: Preferences = koinInject(),
) {
    val isOneway = remember(element) { isOneway(element.tags) }
    val isReversedOneway = remember(element) { isReversedOneway(element.tags) }
    val geometryRotation = remember(geometry) { geometry.getOrientationOrZero() }
    val mapRotation = LocalMapRotation.current
    val mapTilt = LocalMapTilt.current

    val edgeLineStyle = remember {
        when {
            countryInfo.edgeLineStyle.contains("short dashes") -> LineStyle.SHORT_DASHES
            countryInfo.edgeLineStyle.contains("dashes") -> LineStyle.DASHES
            else -> LineStyle.CONTINUOUS
        }
    }
    val edgeLineColor = remember {
        if (countryInfo.edgeLineStyle.contains("yellow")) Color.Yellow else Color.White
    }
    val centerLineColor = remember {
        if (countryInfo.centerLineStyle.contains("yellow")) Color.Yellow else Color.White
    }

    val initialOnewayLaneCount = remember(element) { laneCountInDirection(element.tags, false) }

    var forward by rememberSerializable(element) {
        mutableStateOf(
            if (isOneway) DestinationLanes(if (initialOnewayLaneCount == 1) 1 else initialOnewayLaneCount)
            else null
        )
    }
    var backward by rememberSerializable(element) { mutableStateOf<DestinationLanes?>(null) }
    var selectedIsBackward by rememberSaveable(element) { mutableStateOf(if (isOneway) false else null) }
    var currentLane by rememberSaveable(element) {
        mutableIntStateOf(if (isOneway && initialOnewayLaneCount == 1) 1 else 0)
    }
    var useSingleLaneInput by rememberSaveable(element) {
        mutableStateOf(isOneway && initialOnewayLaneCount == 1)
    }
    var currentText by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }

    var nearbySuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(geometry) {
        nearbySuggestions = nearbyDestinationSuggestions(mapDataSource, geometry)
    }

    fun laneCount(isBackward: Boolean) = laneCountInDirection(element.tags, isBackward)

    fun lanesFor(isBackward: Boolean) = if (isBackward) backward else forward

    fun setLanes(isBackward: Boolean, lanes: DestinationLanes?) {
        if (isBackward) backward = lanes else forward = lanes
    }

    fun ensureLanes(isBackward: Boolean, count: Int): DestinationLanes {
        val existing = lanesFor(isBackward)
        if (existing != null && existing.count == count) return existing
        val created = DestinationLanes(count)
        setLanes(isBackward, created)
        return created
    }

    fun addTo(isBackward: Boolean, lane: Int, raw: String, lanes: DestinationLanes?): DestinationLanes? {
        val dest = raw.trim()
        if (dest.isBlank() || lane == 0) return lanes
        val count = if (useSingleLaneInput) 1 else laneCount(isBackward)
        return (lanes ?: DestinationLanes(count)).add(lane, dest)
    }

    fun selectDirection(isBackward: Boolean) {
        val previous = selectedIsBackward
        if (previous != null && previous != isBackward) {
            val pending = currentText.text
            if (pending.isNotBlank() && currentLane != 0) {
                setLanes(previous, addTo(previous, currentLane, pending, lanesFor(previous)))
            }
            currentText = TextFieldValue()
        }
        selectedIsBackward = isBackward
        val count = laneCount(isBackward)
        if (count == 1) {
            useSingleLaneInput = true
            currentLane = 1
            ensureLanes(isBackward, 1)
        } else {
            useSingleLaneInput = false
            ensureLanes(isBackward, count)
            currentLane = 0
        }
    }

    val selectedSideIsBackward = selectedIsBackward

    fun currentDestinations(): List<String> {
        if (selectedSideIsBackward == null || currentLane == 0) return emptyList()
        return lanesFor(selectedSideIsBackward)?.get(currentLane).orEmpty()
    }

    fun sideComplete(lanes: DestinationLanes?, isSelectedSide: Boolean): Boolean {
        if (lanes?.isComplete == true) return true
        if (!isSelectedSide || currentLane == 0) return false
        return currentText.text.isNotBlank() && lanes?.isCompleteExcept(currentLane) == true
    }

    fun sideHasContent(lanes: DestinationLanes?, isSelectedSide: Boolean): Boolean {
        if (lanes != null && !lanes.isEmpty) return true
        return isSelectedSide && (currentText.text.isNotBlank() || currentDestinations().isNotEmpty())
    }

    val forwardComplete = sideComplete(forward, selectedSideIsBackward == false)
    val backwardComplete = sideComplete(backward, selectedSideIsBackward == true)
    val forwardEmpty = !sideHasContent(forward, selectedSideIsBackward == false)
    val backwardEmpty = !sideHasContent(backward, selectedSideIsBackward == true)

    val pendingTooLong = selectedSideIsBackward?.let { isBackward ->
        if (currentLane == 0) false
        else (lanesFor(isBackward) ?: DestinationLanes(if (useSingleLaneInput) 1 else laneCount(isBackward)))
            .add(currentLane, currentText.text)
            .isTooLong()
    } == true

    val isComplete = ((forwardComplete && backwardComplete)
        || (forwardComplete && backwardEmpty)
        || (forwardEmpty && backwardComplete))
        && !pendingTooLong
        && listOfNotNull(forward, backward).none { it.isTooLong() }

    val hasChanges = isComplete
        || currentText.text.isNotBlank()
        || forward?.isEmpty == false
        || backward?.isEmpty == false

    val lastPicked = remember {
        prefs.getLastPicked<String>(LAST_PICKED_KEY).takeFavorites(20, 50, 1)
    }
    val suggestions = remember(currentText, nearbySuggestions, forward, backward, currentLane, selectedIsBackward) {
        val current = currentDestinations().toSet()
        (forward?.getDestinations().orEmpty() + backward?.getDestinations().orEmpty() + lastPicked + nearbySuggestions)
            .filter { it.startsWith(currentText.text, ignoreCase = true) && it !in current }
            .distinct()
    }

    QuestForm(
        on = on,
        isComplete = isComplete,
        hasChanges = hasChanges,
        onClickOk = {
            var answerForward = forward
            var answerBackward = backward
            val pending = currentText.text
            if (selectedSideIsBackward != null && currentLane != 0 && pending.isNotBlank()) {
                val updated = addTo(selectedSideIsBackward, currentLane, pending, lanesFor(selectedSideIsBackward))
                if (selectedSideIsBackward) answerBackward = updated else answerForward = updated
            }
            answerForward = answerForward?.takeIf { it.isComplete }
            answerBackward = answerBackward?.takeIf { it.isComplete }
            if (answerForward == null && answerBackward == null) return@QuestForm
            (answerForward?.getDestinations().orEmpty() + answerBackward?.getDestinations().orEmpty())
                .distinct()
                .forEach { prefs.addLastPicked(LAST_PICKED_KEY, it) }
            on(Answer(answerForward to answerBackward))
        },
        hintText = if (!isOneway) stringResource(Res.string.quest_street_side_puzzle_tutorial) else null,
        contentPadding = PaddingValues.Zero,
    ) {
        Column(Modifier.fillMaxWidth()) {
            if (!isOneway) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    LanesSelect(
                        value = Lanes(
                            forward = laneCount(false),
                            backward = laneCount(true)
                        ),
                        onClickForwardSide = { selectDirection(false) },
                        onClickBackwardSide = { selectDirection(true) },
                        modifier = Modifier.align(Alignment.Center),
                        rotation = geometryRotation - mapRotation,
                        centerLineColor = centerLineColor,
                        edgeLineColor = edgeLineColor,
                        edgeLineStyle = edgeLineStyle,
                        isLeftHandTraffic = countryInfo.isLeftHandTraffic,
                        isOneway = false,
                        isReversedOneway = isReversedOneway,
                    )
                    MiniCompass(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        rotation = -mapRotation,
                        tilt = mapTilt
                    )
                }
            }

            if (selectedSideIsBackward != null) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val actualLaneCount = laneCount(selectedSideIsBackward)
                    if (actualLaneCount > 1 && !useSingleLaneInput) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = false,
                                onClick = {
                                    val pending = currentText.text
                                    useSingleLaneInput = true
                                    var lanes = DestinationLanes(1)
                                    if (pending.isNotBlank()) lanes = lanes.add(1, pending)
                                    setLanes(selectedSideIsBackward, lanes)
                                    currentLane = 1
                                    currentText = TextFieldValue()
                                }
                            ) {
                                Text(stringResource(Res.string.quest_destination_all_lanes_button))
                            }
                            repeat(actualLaneCount) { idx ->
                                val lane = idx + 1
                                val filled = lanesFor(selectedSideIsBackward)?.get(lane).orEmpty().isNotEmpty()
                                FilterChip(
                                    selected = currentLane == lane,
                                    onClick = {
                                        val pending = currentText.text
                                        if (pending.isNotBlank() && currentLane != 0) {
                                            setLanes(
                                                selectedSideIsBackward,
                                                addTo(selectedSideIsBackward, currentLane, pending, lanesFor(selectedSideIsBackward))
                                            )
                                            currentText = TextFieldValue()
                                        }
                                        ensureLanes(selectedSideIsBackward, actualLaneCount)
                                        currentLane = lane
                                    }
                                ) {
                                    Text(if (filled) "$lane ✓" else lane.toString())
                                }
                            }
                        }
                    }

                    if (currentLane != 0) {
                        DestinationValuesInput(
                            values = currentDestinations(),
                            currentValue = currentText,
                            onCurrentValueChange = { currentText = it },
                            onAdd = { raw ->
                                val dest = raw.trim()
                                if (dest.isBlank()) return@DestinationValuesInput
                                setLanes(
                                    selectedSideIsBackward,
                                    addTo(selectedSideIsBackward, currentLane, dest, lanesFor(selectedSideIsBackward))
                                )
                                currentText = TextFieldValue()
                            },
                            onSelectExisting = { value ->
                                val lanes = lanesFor(selectedSideIsBackward) ?: return@DestinationValuesInput
                                currentText = TextFieldValue(value)
                                setLanes(selectedSideIsBackward, lanes.set(currentLane, lanes.get(currentLane) - value))
                            },
                            suggestions = suggestions,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DestinationValuesInput(
    values: List<String>,
    currentValue: TextFieldValue,
    onCurrentValueChange: (TextFieldValue) -> Unit,
    onAdd: (String) -> Unit,
    onSelectExisting: (String) -> Unit,
    suggestions: List<String>,
) {
    val latestText = remember { mutableStateOf(currentValue.text) }
    Column(Modifier.fillMaxWidth()) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            values.forEach { value ->
                Button({ onSelectExisting(value) }) { Text(value) }
            }
        }
        AutoCompleteTextField(
            value = currentValue,
            onValueChange = {
                latestText.value = it.text
                if (it.text.contains('\n')) {
                    onAdd(it.text)
                } else {
                    onCurrentValueChange(it)
                }
            },
            suggestions = suggestions,
            textStyle = MaterialTheme.typography.largeInput,
            startExpanded = true,
            startExpandedWithoutFocus = true,
            onSelectedSuggestion = { onAdd(latestText.value) },
            modifier = Modifier.fillMaxWidth()
        )
        TextButton(
            onClick = { onAdd(currentValue.text) },
            enabled = currentValue.text.isNotBlank() && currentValue.text.trim() !in values
        ) {
            Text(stringResource(Res.string.quest_destination_add_more))
        }
    }
}

private fun nearbyDestinationSuggestions(
    mapDataSource: MapDataWithEditsSource,
    geometry: ElementGeometry
): List<String> {
    val data = mapDataSource.getMapDataWithGeometry(geometry.bounds.enlargedBy(100.0))
    val suggestions = linkedSetOf<String>()
    data.filter("ways, relations with destination or destination:forward or destination:backward or destination:lanes")
        .forEach {
            it.tags["destination"]?.let { value -> suggestions.addAll(value.split(";")) }
            it.tags["destination:forward"]?.let { value -> suggestions.addAll(value.split(";")) }
            it.tags["destination:backward"]?.let { value -> suggestions.addAll(value.split(";")) }
            it.tags["destination:lanes"]?.let { value -> suggestions.addAll(value.split(";", "|")) }
        }
    return suggestions.map { it.trim() }.filter { it.isNotEmpty() }
}
