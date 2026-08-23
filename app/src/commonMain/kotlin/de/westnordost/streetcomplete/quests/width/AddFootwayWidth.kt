package de.westnordost.streetcomplete.quests.width

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.util.measure.ArSupportChecker

class AddFootwayWidth(
    private val checkArSupport: ArSupportChecker
) : OsmFilterQuestType<WidthAnswer>() {

    /* All either exclusive footways or ways that are cycleway + footway (or bridleway) but
     *  segregated */
    override val elementFilter = """
        ways with (
          (
            highway = footway
            and footway !~ link|crossing
            and bicycle !~ yes|designated
            and (!width or source:width ~ ".*estimat.*")
          ) or (
            segregated = yes
            and (
              highway = cycleway and foot ~ yes|designated
              or highway ~ path|footway and bicycle != no
              or highway = bridleway and bicycle ~ designated|yes
            )
            and (!footway:width or source:footway:width ~ ".*estimat.*")
          )
        )
        and area != yes
        and access !~ private|no
        and placement != transition
        and ~path|footway|cycleway|bridleway !~ link
    """
    override val changesetComment = "Specify footway width"
    override val wikiLink = "Key:width"
    override val icon = Res.drawable.ic_quest_footway_width
    override val title = Res.string.quest_footway_width_title
    override val achievements = listOf(PEDESTRIAN)
    override val defaultDisabledMessage
        get() = if (!checkArSupport()) Res.string.default_disabled_msg_no_ar else Res.string.default_disabled_msg_ee

    @Composable
    override fun Form(on: (QuestAction<WidthAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        AddWidthForm(on, element, countryInfo)
    }

    override fun applyAnswerTo(answer: WidthAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        val isExclusive = tags["highway"] == "footway" && tags["bicycle"] != "yes" && tags["bicycle"] != "designated"

        val key = if (isExclusive) "width" else "footway:width"

        tags[key] = answer.width.toOsmValue()
        if (answer.isARMeasurement) {
            tags["source:$key"] = "ARCore"
        } else {
            tags.remove("source:$key")
        }
    }
}
