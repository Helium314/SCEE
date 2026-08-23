package de.westnordost.streetcomplete.quests.lamp_mount

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

class AddLampMount : OsmFilterQuestType<LampMountAnswer>() {

    override val elementFilter = """
        nodes with
          highway = street_lamp
          and !lamp_mount
          and !support
    """
    override val changesetComment = "Add lamp mount"
    override val defaultDisabledMessage = Res.string.quest_lampMount_disabled_msg
    override val wikiLink = "Key:lamp_mount"
    override val icon = Res.drawable.ic_quest_lamp_mount
    override val title = Res.string.quest_lampMount_title
    override val achievements = listOf(EditTypeAchievement.CITIZEN)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes with highway = street_lamp")

    @Composable
    override fun Form(on: (QuestAction<LampMountAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm<LampMountAnswer>(
            on = on,
            items = LampMount.entries + Support.entries,
            itemContent = { Text(stringResource(it.title)) }
        )
    }

    override fun applyAnswerTo(answer: LampMountAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is LampMount -> {
                tags["lamp_mount"] = answer.mount
            }
            is Support -> {
                tags["support"] = answer.mount
            }
        }
    }
}

