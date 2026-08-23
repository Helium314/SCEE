package de.westnordost.streetcomplete.quests.piste_lit

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.quests.getPrefixedFullElementSelectionPref
import de.westnordost.streetcomplete.util.isWinter
import de.westnordost.streetcomplete.util.ktx.toYesNo
import de.westnordost.streetcomplete.quests.FullElementSelectionDialog
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.YesNoQuestForm

class AddPisteLit : OsmElementQuestType<Boolean> {

    private val elementFilter = """
        ways, relations with
          piste:type ~ downhill|nordic|sled|ski_jump|ice_skate
        and (
          !piste:lit
          or piste:lit older today -16 years
        )
    """
    private val filter by lazy { prefs.getString(getPrefixedFullElementSelectionPref(prefs), elementFilter).toElementFilterExpression() }

    override val changesetComment = "Specify whether pistes are lit"
    override val wikiLink = "Key:piste:lit"
    override val title = Res.string.quest_piste_lit_title
    override val icon = Res.drawable.ic_quest_piste_lit
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        return if (isWinter(mapData.nodes.firstOrNull()?.position))
            mapData.filter(filter)
                // ways only if highway, see https://github.com/Helium314/SCEE/pull/495#issuecomment-3449318516
                .filter { it !is Way || it.tags.contains("highway") }
                .asIterable()
        else emptyList()
    }

    override fun isApplicableTo(element: Element) = if (filter.matches(element)) null else false

    @Composable
    override fun Form(on: (QuestAction<Boolean>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        YesNoQuestForm(on)
    }

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("piste:lit", answer.toYesNo())
    }

    override val hasQuestSettings: Boolean = true

    @Composable
    override fun QuestSettings(onDismissRequest: () -> Unit) {
        FullElementSelectionDialog(prefs, getPrefixedFullElementSelectionPref(prefs), Res.string.quest_settings_element_selection, elementFilter, onDismissRequest)
    }
}
