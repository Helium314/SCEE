package de.westnordost.streetcomplete.quests.fence_material

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.bridge_structure.BridgeStructure
import de.westnordost.streetcomplete.quests.bridge_structure.icon
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_ee
import de.westnordost.streetcomplete.resources.ic_quest_fence_material
import de.westnordost.streetcomplete.resources.quest_fence_material_title
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddFenceMaterial : OsmFilterQuestType<FenceMaterial>() {

    override val elementFilter = """
        nodes, ways, relations with
          barrier = fence
          and !material
          and fence_type != wire
    """

    override val changesetComment = "Specify fence material"
    override val wikiLink = "Key:material"
    override val title = Res.string.quest_fence_material_title
    override val icon = Res.drawable.ic_quest_fence_material

    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    @Composable
    override fun Form(on: (QuestAction<FenceMaterial>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            on = on,
            items = FenceMaterial.entries,
            itemContent = { ImageWithLabel(painterResource(it.imageResId), stringResource(it.titleResId)) },
        )
    }

    override fun applyAnswerTo(
        answer: FenceMaterial,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long,
    ) {
        answer.materialValue?.let { tags["material"] = it }
        answer.fenceTypeValue?.let { tags["fence_type"] = it }
    }
}
