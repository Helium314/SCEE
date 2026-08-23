package de.westnordost.streetcomplete.quests.paving_stones_material

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.surface.getKeysAssociatedWithSurface
import de.westnordost.streetcomplete.osm.removeCheckDatesForKey
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddPavingStonesMaterial : OsmFilterQuestType<PavingStonesMaterialAnswer>() {

    override val elementFilter = """
        ways with
          surface=paving_stones
          and !paving_stones:material
    """
    override val changesetComment = "Specify paving stones material"
    override val wikiLink = "Key:paving_stones:material"
    override val icon = Res.drawable.quest_paving_stones_material
    override val title = Res.string.quest_pavingStonesMaterial_title
    override val defaultDisabledMessage = Res.string.default_disabled_msg_difficult_and_time_consuming

    @Composable
    override fun Form(on: (QuestAction<PavingStonesMaterialAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            on = on,
            items = PavingStonesMaterial.entries,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            otherAnswers = {
                listOf(
                    AnswerItem(stringResource(Res.string.quest_smoothness_wrong_surface)) {
                        on(Answer(SurfaceIsNotPavingStones))
                    },
                )
            }
        )
    }

    override fun applyAnswerTo(
        answer: PavingStonesMaterialAnswer,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long,
    ) {
        when (answer) {
            is PavingStonesMaterial -> tags["paving_stones:material"] = answer.osmValue
            SurfaceIsNotPavingStones -> {
                tags.remove("surface")
                tags.removeCheckDatesForKey("surface")
                getKeysAssociatedWithSurface().forEach { tags.remove(it) }
            }
        }
    }
}
