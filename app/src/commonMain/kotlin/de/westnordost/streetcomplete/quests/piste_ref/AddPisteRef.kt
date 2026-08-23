package de.westnordost.streetcomplete.quests.piste_ref

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.getPrefixedFullElementSelectionPref
import de.westnordost.streetcomplete.util.isWinter
import de.westnordost.streetcomplete.quests.FullElementSelectionDialog
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.AreYouSureDialog
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.TextInputForm
import org.jetbrains.compose.resources.stringResource

class AddPisteRef : OsmElementQuestType<PisteRefAnswer> {

    private val elementFilter = """
        ways, relations with
          piste:type = downhill
          and !ref
          and !piste:ref
    """
    private val filter by lazy { prefs.getString(getPrefixedFullElementSelectionPref(prefs), elementFilter).toElementFilterExpression() }

    override val changesetComment = "Survey piste ref"
    override val wikiLink = "Key:piste:ref"
    override val icon = Res.drawable.ic_quest_piste_ref
    override val title = Res.string.quest_piste_ref_title
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        return if (isWinter(mapData.nodes.firstOrNull()?.position)) mapData.filter(filter).asIterable()
            else emptyList()
    }

    override fun isApplicableTo(element: Element) = if (filter.matches(element)) null else false

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("ways, relations with piste:type = downhill")

    @Composable
    override fun Form(on: (QuestAction<PisteRefAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        var isConnection by remember { mutableStateOf(false) }
        TextInputForm(
            on,
            stringToAnswer = { PisteRef(it) },
            otherAnswers = { listOf(AnswerItem(stringResource(Res.string.quest_piste_ref_connection)) { isConnection = true }) }
        )
        if (isConnection)
            AreYouSureDialog(
                { isConnection = false },
                { on(Answer(PisteConnection)) }
            )
        /*
        todo: circle around text field and color for piste:difficulty
        private fun getColorForPisteDifficulty(difficulty: String?): Int {
        return when (difficulty) {
            "novice" -> Color.parseColor("#008351")
            "easy" -> Color.parseColor("#2255BB")
            "intermediate" -> Color.parseColor("#C1121C")
            "advanced" -> Color.parseColor("#000000")
            else -> Color.parseColor("#8e9291")
        }
    }
         */
    }

    override fun applyAnswerTo(answer: PisteRefAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is PisteRef ->          tags["piste:ref"] = answer.ref
            is PisteConnection ->   tags["piste:type"] = "connection"
        }
    }

    override val hasQuestSettings: Boolean = true

    @Composable
    override fun QuestSettings(onDismissRequest: () -> Unit) {
        FullElementSelectionDialog(prefs, getPrefixedFullElementSelectionPref(prefs), Res.string.quest_settings_element_selection, elementFilter, onDismissRequest)
    }
}
