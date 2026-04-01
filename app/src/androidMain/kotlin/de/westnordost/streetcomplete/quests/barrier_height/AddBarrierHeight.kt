package de.westnordost.streetcomplete.quests.barrier_height

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.screens.measure.ArSupportChecker
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_ee
import de.westnordost.streetcomplete.resources.default_disabled_msg_no_ar
import de.westnordost.streetcomplete.resources.quest_barrier_height_title

class AddBarrierHeight(
    private val checkArSupport: ArSupportChecker
) : OsmFilterQuestType<BarrierHeightAnswer>(), AndroidQuest {

    override val elementFilter = """
        ways with
        barrier ~ fence|guard_rail|handrail|hedge|wall|cable_barrier
        and !height
    """

    override val changesetComment = "Specify barrier heights"
    override val wikiLink = "Key:height"
    override val icon = R.drawable.ic_quest_barrier_height
    override val title = Res.string.quest_barrier_height_title
    override val achievements = listOf(EditTypeAchievement.PEDESTRIAN)
    override val defaultDisabledMessage
        get() = if (!checkArSupport()) Res.string.default_disabled_msg_no_ar else Res.string.default_disabled_msg_ee

    override fun createForm() = AddBarrierHeightForm()

    override fun applyAnswerTo(answer: BarrierHeightAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["height"] = answer.height.toOsmValue()
        if (answer.isARMeasurement) {
            tags["source:height"] = "ARCore"
        } else {
            tags.remove("source:height")
        }
    }
}
