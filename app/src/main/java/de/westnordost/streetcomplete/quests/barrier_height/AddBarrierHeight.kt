package de.westnordost.streetcomplete.quests.barrier_height

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.screens.measure.ArSupportChecker

class AddBarrierHeight(
    private val checkArSupport: ArSupportChecker
) : OsmElementQuestType<BarrierHeightAnswer> {

    private val wayFilter by lazy { """
        ways with
        barrier ~ fence|guard_rail|handrail|hedge|wall|cable_barrier|retaining_wall
        and !height
    """.toElementFilterExpression() }

    override val changesetComment = "Specify barrier heights"
    override val wikiLink = "Key:height"
    override val icon = R.drawable.ic_quest_barrier_height
    override val achievements = listOf(EditTypeAchievement.PEDESTRIAN)
    override val defaultDisabledMessage: Int
        get() = if (!checkArSupport()) R.string.default_disabled_msg_no_ar else R.string.default_disabled_msg_ee

    override fun getTitle(tags: Map<String, String>): Int {
        return R.string.quest_barrier_height_title
    }

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.ways.filter { wayFilter.matches(it) }

    override fun isApplicableTo(element: Element): Boolean =
        wayFilter.matches(element)

    override fun createForm() = AddBarrierHeightForm()

    override fun applyAnswerTo(answer: BarrierHeightAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        // overwrite maxheight value but retain the info that there is no sign onto another tag
        tags["height"] = answer.height.toOsmValue()
        if (answer.isARMeasurement) {
            tags["source:height"] = "ARCore"
        } else {
            tags.remove("source:height")
        }
    }
}
