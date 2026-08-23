package de.westnordost.streetcomplete.quests.kerb_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BLIND
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddKerbType : OsmFilterQuestType<KerbType>() {

    override val elementFilter = """
        ways with
          barrier = kerb
          and !kerb
    """.trimIndent()

    override val changesetComment = "Specify kerb types"
    override val wikiLink = "Key:kerb"
    override val icon = Res.drawable.quest_kerb_type
    override val title = Res.string.quest_kerb_type_title
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee
    override val achievements = listOf(BLIND, WHEELCHAIR, BICYCLIST)

    @Composable
    override fun Form(on: (QuestAction<KerbType>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            on = on,
            items = KerbType.entries,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            itemsPerRow = 2,
        )
    }

    override fun applyAnswerTo(answer: KerbType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("kerb", answer.osmValue)
        if (answer == KerbType.NO_KERB) {
            tags.remove("barrier")
            tags["no:barrier"] = "kerb"
        }
    }
}
