package de.westnordost.streetcomplete.quests.building_colour

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import de.westnordost.streetcomplete.util.image.toPainter
import de.westnordost.streetcomplete.util.toResId
import org.jetbrains.compose.resources.stringResource

class AddBuildingColour : OsmFilterQuestType<BuildingColour>() {

    override val elementFilter = """
        ways, relations with
          ((building and building !~ no|construction|roof|carport)
          or (building:part and building:part !~ no|construction|roof|carport))
          and !building:colour
          and (!indoor or indoor = no)
          and wall !~ no
          and location != underground
    """
    override val changesetComment = "Specify building colour"
    override val wikiLink = "Key:building:colour"
    override val title = Res.string.quest_buildingColour_title
    override val icon = Res.drawable.ic_quest_building_colour
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    @Composable
    override fun Form(on: (QuestAction<BuildingColour>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        val ctx = LocalContext.current
        val iconId = Res.drawable.ic_building_colour.toResId(ctx)
        ItemSelectQuestForm(
            on = on,
            items = BuildingColour.entries,
            itemContent = { Image(it.getDrawable(ctx, iconId).toPainter(), null) },
            itemsPerRow = 4,
            title = stringResource(if (element.tags.containsKey("building:part")) Res.string.quest_buildingPartColour_title
                else Res.string.quest_buildingColour_title)
        )
    }

    override fun applyAnswerTo(
        answer: BuildingColour,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long,
    ) {
        tags["building:colour"] = answer.osmValue
    }
}

