package de.westnordost.streetcomplete.quests.orchard_type

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddOrchardType : OsmFilterQuestType<OrchardType>() {

    override val elementFilter = """
        ways, relations with landuse = orchard and !orchard
    """
    override val changesetComment = "Specify orchard type"
    override val wikiLink = "Key:orchard"
    override val icon = Res.drawable.quest_apple
    override val title = Res.string.quest_orchard_type_title
    override val achievements = listOf(OUTDOORS)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    @Composable
    override fun Form(on: (QuestAction<OrchardType>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            on = on,
            items = OrchardType.entries,
            itemContent = {
                ImageWithDescription(
                    painter = painterResource(it.icon),
                    title = stringResource(it.title),
                    description = stringResource(it.description),
                    imageSize = DpSize(64.dp, 64.dp)
                )
            },
            itemsPerRow = 1,
        )
    }

    override fun applyAnswerTo(answer: OrchardType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["orchard"] = answer.osmValue
    }
}
