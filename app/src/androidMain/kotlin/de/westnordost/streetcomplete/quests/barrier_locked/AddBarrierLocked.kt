package de.westnordost.streetcomplete.quests.barrier_locked

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*

class AddBarrierLocked : OsmFilterQuestType<BarrierLockedAnswer>(), AndroidQuest {

    // We keep nodes and ways because many barriers are mapped as ways in OSM.
    override val elementFilter = """
        nodes, ways with
          barrier ~ bump_gate|chain|door|gate|swing_gate|sliding_gate|sliding_beam|wicket_gate
        and (
          !locked
          or locked = yes and locked older today -5 years
          or locked older today -10 years
        )
    """

    override val changesetComment = "Add whether barriers are locked"
    override val wikiLink = "Key:locked"
    override val icon = R.drawable.ic_quest_barrier_locked
    override val title = Res.string.quest_barrier_locked_title
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    override fun createForm() = AddBarrierLockedForm()

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val base = super.getApplicableElements(mapData)

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
