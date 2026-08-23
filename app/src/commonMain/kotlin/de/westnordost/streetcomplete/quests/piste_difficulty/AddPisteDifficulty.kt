package de.westnordost.streetcomplete.quests.piste_difficulty

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.getPrefixedFullElementSelectionPref
import de.westnordost.streetcomplete.util.isWinter
import de.westnordost.streetcomplete.quests.FullElementSelectionDialog
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddPisteDifficulty : OsmElementQuestType<PisteDifficulty> {

    val elementFilter = """
        ways, relations with
          piste:type ~ downhill|nordic
          and !piste:difficulty
    """
    private val filter by lazy { prefs.getString(getPrefixedFullElementSelectionPref(prefs), elementFilter).toElementFilterExpression() }

    override val changesetComment = "Add piste difficulty"
    override val wikiLink = "Key:piste:difficulty"
    override val title = Res.string.quest_piste_difficulty_title
    override val icon = Res.drawable.ic_quest_piste_difficulty
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        return if (isWinter(mapData.nodes.firstOrNull()?.position)) mapData.filter(filter).asIterable()
            else emptyList()
    }

    override fun isApplicableTo(element: Element) = if (filter.matches(element)) null else false

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry): Sequence<Element> {
        return mapData.filter("ways, relations with piste:type")
    }

    @Composable
    override fun Form(on: (QuestAction<PisteDifficulty>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            on = on,
            items = PisteDifficulty.entries,
            itemContent = {
                ImageWithLabel(
                    painter = painterResource(it.getIcon(countryInfo.countryCode ?: "")),
                    label = stringResource(it.title),
                )
            },
            itemsPerRow = 2,
        )
    }

    override fun applyAnswerTo(answer: PisteDifficulty, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["piste:difficulty"] = answer.osmValue
    }

    override val hasQuestSettings: Boolean = true

    @Composable
    override fun QuestSettings(onDismissRequest: () -> Unit) {
        FullElementSelectionDialog(prefs, getPrefixedFullElementSelectionPref(prefs), Res.string.quest_settings_element_selection, elementFilter, onDismissRequest)
    }
}
