package de.westnordost.streetcomplete.quests.barrier_height

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.length.Length
import de.westnordost.streetcomplete.osm.length.LengthForm
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.measure.ArMeasureResult
import de.westnordost.streetcomplete.ui.util.measure.ArMeasureViewModel
import de.westnordost.streetcomplete.ui.util.measure.ArSupportChecker
import de.westnordost.streetcomplete.ui.util.measure.LastArMeasurementResultEffect
import de.westnordost.streetcomplete.ui.util.measure.rememberArMeasureAppLauncher
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.koin.compose.viewmodel.koinViewModel

class AddBarrierHeight(
    private val checkArSupport: ArSupportChecker
) : OsmFilterQuestType<BarrierHeightAnswer>() {

    override val elementFilter = """
        ways with
        barrier ~ fence|guard_rail|handrail|hedge|wall|cable_barrier
        and !height
    """

    override val changesetComment = "Specify barrier heights"
    override val wikiLink = "Key:height"
    override val icon = Res.drawable.ic_quest_barrier_height
    override val title = Res.string.quest_barrier_height_title
    override val achievements = listOf(EditTypeAchievement.PEDESTRIAN)
    override val defaultDisabledMessage
        get() = if (!checkArSupport()) Res.string.default_disabled_msg_no_ar else Res.string.default_disabled_msg_ee

    @Composable
    override fun Form(on: (QuestAction<BarrierHeightAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        val viewModel = koinViewModel<ArMeasureViewModel>()
        val arIsSupported = remember { viewModel.isSupported() }
        val arMeasureAppLauncher = rememberArMeasureAppLauncher()

        var length by rememberSerializable { mutableStateOf<Length?>(null) }
        var lastArMeasurementResult by remember { mutableStateOf<ArMeasureResult?>(null) }
        var isArMeasurement by rememberSaveable { mutableStateOf<Boolean>(false) }

        LastArMeasurementResultEffect(
            lastResult = lastArMeasurementResult,
            onConfirmDisableArQuests = {
                viewModel.disableArQuests()
                lastArMeasurementResult = null
            }
        )

        fun onMeasureResult(result: ArMeasureResult) {
            if (result is ArMeasureResult.Success) {
                length = result.length
                isArMeasurement = true
            }
        }

        QuestForm(
            on = on,
            isComplete = length != null,
            onClickOk = { on(Answer(BarrierHeightAnswer(length!!, isArMeasurement))) },
        ) {
            LengthForm(
                length = length,
                onChange = {
                    isArMeasurement = false
                    length = it
                },
                selectableUnits = countryInfo.lengthUnits,
                showMeasureButton = arIsSupported,
                onClickMeasure = { lengthUnit ->
                    arMeasureAppLauncher.measure(
                        lengthUnit = lengthUnit,
                        measureVertical = true,
                        onResult = ::onMeasureResult
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    override fun applyAnswerTo(answer: BarrierHeightAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["height"] = answer.height.toOsmValue()
        if (answer.isARMeasurement) {
            tags["source:height"] = "ARCore"
        } else {
            tags.remove("source:height")
        }
    }
}
