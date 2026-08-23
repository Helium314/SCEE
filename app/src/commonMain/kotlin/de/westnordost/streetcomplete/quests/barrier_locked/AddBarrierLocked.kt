package de.westnordost.streetcomplete.quests.barrier_locked

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import de.westnordost.osm_opening_hours.model.Month
import de.westnordost.osm_opening_hours.model.MonthRange
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.opening_hours.HierarchicOpeningHours
import de.westnordost.streetcomplete.osm.time_restriction.TimeRestriction
import de.westnordost.streetcomplete.osm.time_restriction.TimeRestrictionInput
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource

class AddBarrierLocked : OsmElementQuestType<BarrierLockedAnswer> {

    // We keep nodes and ways because many barriers are mapped as ways in OSM.
    val elementFilter by lazy { """
        nodes, ways with
          barrier ~ bump_gate|chain|door|gate|swing_gate|sliding_gate|sliding_beam|wicket_gate
        and (
          !locked
          or locked = yes and locked older today -5 years
          or locked older today -10 years
        )
    """.toElementFilterExpression() }

    override val changesetComment = "Add whether barriers are locked"
    override val wikiLink = "Key:locked"
    override val icon = Res.drawable.ic_quest_barrier_locked
    override val title = Res.string.quest_barrier_locked_title
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    @Composable
    override fun Form(on: (QuestAction<BarrierLockedAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        var answer by rememberSerializable { mutableStateOf<BarrierLockedAnswer?>(null) }

        if (answer == null)
            QuestForm(
                on,
                answers = listOf(
                    AnswerItem(stringResource(Res.string.quest_generic_hasFeature_no)) { on(Answer(NotLocked)) },
                        AnswerItem(stringResource(Res.string.quest_generic_hasFeature_yes)) { on(Answer(Locked)) }
                ),
                otherAnswers = { listOf(
                    AnswerItem(stringResource(Res.string.quest_fee_answer_hours)) {
                        answer = LockedAtHours(TimeRestriction(HierarchicOpeningHours(),TimeRestriction.Mode.ONLY_AT_HOURS))
                    },
                    AnswerItem(stringResource(Res.string.quest_openingHours_answer_seasonal_opening_hours)) {
                        val old = (answer as? LockedAtHours)?.timeRestriction ?: TimeRestriction(HierarchicOpeningHours(),TimeRestriction.Mode.ONLY_AT_HOURS)
                        val allMonths = listOf(MonthRange(Month.January, Month.December))
                        val hours = HierarchicOpeningHours(
                            old.hours.monthsList.map { months ->
                                if (months.selectors.isEmpty()) months.copy(selectors = allMonths) else months
                            }
                        )
                        answer = LockedAtHours(TimeRestriction(hours, old.mode))
                    }
                ) }
            )
        else
            QuestForm(
                on,
                isComplete = (answer as? LockedAtHours)?.timeRestriction?.isComplete() == true,
                onClickOk = { answer?.let { on(Answer(it)) } },
            ) {
                val answer2 = answer
                if (answer2 is LockedAtHours) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(stringResource(Res.string.quest_fee_answer_yes_but))
                        TimeRestrictionInput(
                            timeRestriction = answer2.timeRestriction,
                            onChange = { answer = LockedAtHours(it) },
                            countryInfo = countryInfo,
                            allowSelectNoRestriction = false,
                        )
                    }
                }
            }
    }

    override fun isApplicableTo(element: Element): Boolean = elementFilter.matches(element)

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val base = getApplicableElements(mapData)

        // Build a lookup from node id to the ways that are connected to it
        val waysByNodeId = mutableMapOf<Long, MutableList<Way>>()
        for (way in mapData.ways) {
            if (way.tags["highway"] == null) continue // restrict to highway ways for relevance
            for (nodeId in way.nodeIds) {
                waysByNodeId.getOrPut(nodeId) { mutableListOf() }.add(way)
            }
        }

        return base
            .filterIsInstance<Node>()
            .filter { node ->
                val connectedWays = waysByNodeId[node.id].orEmpty()

                // optional small short-circuit: if fewer than 2 ways, cannot match (1,1)
                if (connectedWays.size < 2) return@filter true

                var restrictedCount = 0
                var noAccessTagCount = 0

                for (way in connectedWays) {
                    val access = way.tags["access"]
                    when (access) {
                        null -> noAccessTagCount++
                        "private", "no" -> restrictedCount++
                    }
                }

                // Exclude nodes where exactly one connected way has access=private|no
                // and exactly one connected way has no access tag.
                !(restrictedCount == 1 && noAccessTagCount == 1)
            }
    }

    override fun applyAnswerTo(answer: BarrierLockedAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.applyTo(tags)
    }
}
