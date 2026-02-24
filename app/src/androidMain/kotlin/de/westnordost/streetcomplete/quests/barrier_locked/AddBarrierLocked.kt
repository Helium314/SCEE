package de.westnordost.streetcomplete.quests.barrier_locked

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_ee

class AddBarrierLocked : OsmElementQuestType<BarrierLockedAnswer>, AndroidQuest {

    // We keep nodes and ways because many barriers are mapped as ways in OSM.
    private val barrierLockedFilterExpression = """
        nodes, ways with
          barrier ~ bump_gate|chain|door|gate|swing_gate|sliding_gate|sliding_beam|wicket_gate
        and (
          !locked
          or locked = yes and locked older today -5 years
          or locked older today -10 years
        )
    """

    // local filter expression derived from elementFilter (used by isApplicableTo)
    private val filter by lazy { barrierLockedFilterExpression.toElementFilterExpression() }

    override val changesetComment = "Add whether barriers are locked"
    override val wikiLink = "Key:locked"
    override val icon = R.drawable.ic_quest_barrier_locked
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    override fun getTitle(tags: Map<String, String>) = R.string.quest_barrier_locked_title

    override fun createForm() = AddBarrierLockedForm()

    override fun isApplicableTo(element: Element): Boolean? {
        if (!filter.matches(element)) return false

        // Element matches the base filter -> we need surrounding map data to decide precisely.
        // Return null so getApplicableElements(mapData) is used for the final decision.
        return null
    }

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        // Start with all elements that match the configured filter string
        val filteredElements = mapData.filter(barrierLockedFilterExpression).asIterable()

        // Build a lookup from node id to the ways that are connected to it
        val waysByNodeId = mutableMapOf<Long, MutableList<Way>>()
        for (way in mapData.ways) {
            if (way.tags["highway"] == null) continue // restrict to highway ways for relevance
            for (nodeId in way.nodeIds) {
                waysByNodeId.getOrPut(nodeId) { mutableListOf() }.add(way)
            }
        }

        val nodeResults = filteredElements
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
                        else -> { /* ignore other access values */ }
                    }
                }

                // Exclude nodes where exactly one connected way has access=private|no
                // and exactly one connected way has no access tag.
                !(restrictedCount == 1 && noAccessTagCount == 1)
            }

        val wayResults = filteredElements
            .filterIsInstance<Way>()

        // Combine results: nodes + ways that passed the checks
        return nodeResults + wayResults
    }

    override fun applyAnswerTo(answer: BarrierLockedAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.applyTo(tags)
    }
}
