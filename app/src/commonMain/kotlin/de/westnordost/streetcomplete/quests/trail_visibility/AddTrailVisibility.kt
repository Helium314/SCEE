package de.westnordost.streetcomplete.quests.trail_visibility

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.sac_scale.SacScale
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddTrailVisibility : OsmFilterQuestType<TrailVisibility>() {

    override val elementFilter = """
        ways with
          highway ~ path|footway|cycleway|bridleway
          and !trail_visibility
          and ( access !~ no|private or foot ~ yes|permissive|designated or bicycle ~ yes|permissive|designated)
          and (sac_scale and sac_scale !~ hiking|strolling)
          and (!lit or lit = no)
          and surface ~ "ground|earth|dirt|soil|grass|sand|mud|ice|salt|snow|rock|stone"
    """
    override val changesetComment = "Specify Trail Visibility"
    override val wikiLink = "Key:trail_visibility"
    override val title = Res.string.quest_trail_visibility_title
    override val icon = Res.drawable.ic_quest_trail_visibility
    override val defaultDisabledMessage = Res.string.default_disabled_msg_trail_visibility

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("ways with highway and trail_visibility")

    @Composable
    override fun Form(on: (QuestAction<TrailVisibility>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            on = on,
            items = TrailVisibility.entries,
            itemContent = {
                ImageWithDescription(null,
                    stringResource(it.titleRes),
                    stringResource(it.descriptionRes)
                )
            },
            itemsPerRow = 2
        )
    }

    override fun applyAnswerTo(answer: TrailVisibility, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["trail_visibility"] = answer.osmValue
    }
}

