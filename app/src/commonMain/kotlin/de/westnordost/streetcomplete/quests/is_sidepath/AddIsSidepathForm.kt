package de.westnordost.streetcomplete.quests.is_sidepath

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.main.map.getIcon
import de.westnordost.streetcomplete.screens.main.map.getTitle
import de.westnordost.streetcomplete.ui.common.RadioGroup
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.LocalMapMarkersCallback
import de.westnordost.streetcomplete.ui.common.quest.Marker
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.getNameLabel
import de.westnordost.streetcomplete.util.ktx.getFeature
import de.westnordost.streetcomplete.util.math.enlargedBy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun AddIsSidepathForm(
    on: (QuestAction<IsSidepathAnswer>) -> Unit,
    element: Element,
    geometry: ElementGeometry,
    mapDataSource: MapDataWithEditsSource = koinInject(),
    featureDictionary: FeatureDictionary = koinInject(),
) {
    var step by rememberSaveable { mutableStateOf(IsSidepathFormStep.AskIfSidepath) }

    when (step) {
        IsSidepathFormStep.AskIfSidepath -> {
            val isFootway = element.tags["highway"] == "footway"
            QuestForm(
                on = on,
                answers = listOfNotNull(
                    if (isFootway) {
                        AnswerItem(stringResource(Res.string.quest_is_sidepath_answer_sidewalk)) {
                            on(Answer(IsSidepathAnswer.IsSidewalk))
                        }
                    } else {
                        null
                    },
                    AnswerItem(stringResource(Res.string.quest_is_sidepath_answer_parallel_path)) {
                        step = IsSidepathFormStep.ChooseRoad
                    },
                    AnswerItem(stringResource(Res.string.quest_generic_hasFeature_no)) {
                        on(Answer(IsSidepathAnswer.No))
                    },
                ),
                otherAnswers = {
                    listOf(
                        AnswerItem(
                            stringResource(Res.string.quest_is_sidepath_answer_is_crossing)
                        ) {
                            on(Answer(IsSidepathAnswer.IsCrossing))
                        },
                    )
                }
            )
        }

        IsSidepathFormStep.ChooseRoad -> {
            ChooseRoadForm(
                on = on,
                pathGeometry = geometry,
                mapDataSource = mapDataSource,
                featureDictionary = featureDictionary,
                onBack = { step = IsSidepathFormStep.AskIfSidepath },
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ChooseRoadForm(
    on: (QuestAction<IsSidepathAnswer>) -> Unit,
    pathGeometry: ElementGeometry,
    mapDataSource: MapDataWithEditsSource,
    featureDictionary: FeatureDictionary,
    onBack: () -> Unit,
) {
    var candidates by rememberSerializable {
        mutableStateOf<List<CandidateRoad>>(emptyList())
    }
    var loaded by rememberSaveable { mutableStateOf(false) }
    var selection by rememberSerializable {
        mutableStateOf<SidepathRoadSelection?>(null)
    }

    LaunchedEffect(pathGeometry.center) {
        loaded = false
        val bbox = pathGeometry.bounds.enlargedBy(
            MAX_SIDEPATH_DISTANCE_METERS + SIDEPATH_MAP_DATA_QUERY_PADDING_METERS
        )
        val mapData = withContext(Dispatchers.IO) {
            mapDataSource.getMapDataWithGeometry(bbox)
        }
        candidates = findCandidateRoads(pathGeometry, mapData)
        loaded = true
    }

    val mapMarkersCallback = LocalMapMarkersCallback.current
    LaunchedEffect(selection, candidates) {
        val selectedId = (selection as? SidepathRoadSelection.Road)?.elementId
        val markers = if (selectedId == null) {
            emptyList()
        } else {
            candidates.filter { it.element.id == selectedId }.map { candidate ->
                Marker(
                    geometry = candidate.geometry,
                    icon = getIcon(featureDictionary, candidate.element),
                    title = getTitle(candidate.element.tags),
                )
            }
        }
        mapMarkersCallback?.invoke(markers)
    }

    fun clearAndGoBack() {
        selection = null
        mapMarkersCallback?.invoke(emptyList())
        onBack()
    }

    QuestForm(
        on = on,
        title = stringResource(Res.string.quest_is_sidepath_road_title),
        isComplete = loaded && selection != null,
        hasChanges = selection != null,
        onClickOk = {
            when (val selected = selection) {
                is SidepathRoadSelection.Road -> {
                    val name = candidates
                        .firstOrNull { it.element.id == selected.elementId }
                        ?.name
                    on(Answer(IsSidepathAnswer.Yes(ofName = name)))
                }
                SidepathRoadSelection.CantSay, null ->
                    on(Answer(IsSidepathAnswer.Yes(ofName = null)))
            }
        },
    ) {
        if (!loaded) {
            Text(stringResource(Res.string.quest_is_sidepath_road_loading))
        } else {
            val options = buildList {
                addAll(candidates.map { SidepathRoadSelection.Road(it.element.id) })
                add(SidepathRoadSelection.CantSay)
            }
            RadioGroup(
                options = options,
                onSelectionChange = { selection = it },
                selectedOption = selection,
                itemContent = { option ->
                    when (option) {
                        is SidepathRoadSelection.Road -> {
                            val candidate = candidates.first { it.element.id == option.elementId }
                            CandidateRoadLabel(candidate, featureDictionary)
                        }
                        SidepathRoadSelection.CantSay -> {
                            Text(stringResource(Res.string.quest_is_sidepath_road_cant_say))
                        }
                    }
                },
            )
        }
    }

    // After QuestForm so this takes precedence over form dismiss / discard.
    BackHandler { clearAndGoBack() }
}

@Composable
private fun CandidateRoadLabel(
    candidate: CandidateRoad,
    featureDictionary: FeatureDictionary,
) {
    val nameLabel = getNameLabel(candidate.element.tags)
    val featureName = featureDictionary.getFeature(candidate.element)?.name
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        if (nameLabel != null) {
            Text(nameLabel)
            if (featureName != null) {
                Text(
                    text = featureName,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                )
            }
        } else if (featureName != null) {
            Text(stringResource(Res.string.quest_is_sidepath_unnamed_road, featureName))
        } else {
            Text(stringResource(Res.string.quest_is_sidepath_unnamed_road, candidate.highway ?: "road"))
        }
    }
}

private enum class IsSidepathFormStep {
    AskIfSidepath,
    ChooseRoad,
}

@Serializable
private sealed interface SidepathRoadSelection {
    @Serializable
    data class Road(val elementId: Long) : SidepathRoadSelection
    @Serializable
    data object CantSay : SidepathRoadSelection
}
