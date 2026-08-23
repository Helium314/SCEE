package de.westnordost.streetcomplete.quests.sac_scale

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapData
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.BooleanQuestSettingsDialog
import de.westnordost.streetcomplete.quests.FullElementSelectionDialog
import de.westnordost.streetcomplete.quests.getPrefixedFullElementSelectionPref
import de.westnordost.streetcomplete.quests.questPrefix
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.InfoDialog
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddSacScale : OsmElementQuestType<SacScale> {

    private val elementFilter = """
        ways with
          highway ~ path
          and !sac_scale
          and access !~ no|private
          and foot !~ no|private
          and (!lit or lit = no)
          and surface ~ "grass|sand|dirt|soil|fine_gravel|compacted|wood|gravel|pebblestone|rock|ground|earth|mud|woodchips|snow|ice|salt|stone"
    """
    private val filter by lazy { prefs.getString(getPrefixedFullElementSelectionPref(prefs), elementFilter).toElementFilterExpression() }

    override val changesetComment = "Specify SAC Scale"
    override val wikiLink = "Key:sac_scale"
    override val icon = Res.drawable.ic_quest_sac_scale
    override val title = Res.string.quest_sacScale_title
    override val defaultDisabledMessage = Res.string.default_disabled_msg_sacScale

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        if (isSacScaleWithoutRelation) {
            mapData.filter(filter).asIterable()
        } else {
            mapData.relations.filter {
                it.tags["route"] == "hiking"
            }.map {
                mapData.getAllWayInRelation(it.id).filter { way ->
                    filter.matches(way)
                }
            }.flatten()
        }


    override fun isApplicableTo(element: Element) = null

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("ways with highway and sac_scale")

    @Composable
    override fun Form(on: (QuestAction<SacScale>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            on = on,
            items = SacScale.entries,
            itemContent = {
                ImageWithDescription(
                    painterResource(it.imageResId),
                    stringResource(it.titleResId),
                    stringResource(it.descriptionResId)
                )
            },
            itemsPerRow = 1
        )
    }

    override fun applyAnswerTo(answer: SacScale, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["sac_scale"] = answer.osmValue
    }

    override val hasQuestSettings: Boolean = true

    @Composable override fun QuestSettings(onDismissRequest: () -> Unit) {
        var showResurveySelection by remember { mutableStateOf(false) }
        var showElementSelection by remember { mutableStateOf(false) }
        InfoDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(stringResource(Res.string.quest_settings_what_to_edit)) },
            text = {
                Column {
                    Button({ showResurveySelection = true }, Modifier.fillMaxWidth()) {
                        Text(stringResource(Res.string.pref_quest_sac_scale_without_relation))
                    }
                    Button({ showElementSelection = true }, Modifier.fillMaxWidth()) {
                        Text(stringResource(Res.string.element_selection_button))
                    }
                }
            }
        )
        if (showResurveySelection)
            BooleanQuestSettingsDialog(
                prefs,
                questPrefix(prefs) + PREF_SAC_SCALE_WITHOUT_RELATION,
                false,
                Res.string.pref_quest_sac_scale_without_relation,
                Res.string.quest_generic_hasFeature_yes,
                Res.string.quest_generic_hasFeature_no,
                onDismissRequest
            )
        if (showElementSelection)
            FullElementSelectionDialog(
                prefs,
                getPrefixedFullElementSelectionPref(prefs),
                Res.string.quest_settings_element_selection,
                elementFilter
            ) { showElementSelection = false }
    }

    private val isSacScaleWithoutRelation = prefs.getBoolean(questPrefix(prefs) + PREF_SAC_SCALE_WITHOUT_RELATION, false)

    private fun MapData.getAllWayInRelation(id: Long): List<Way> {
        val mutableList = mutableListOf<Way>()

        getRelation(id)?.members?.forEach { member ->
            when (member.type) {
                ElementType.NODE -> Unit
                ElementType.WAY -> getWay(member.ref)?.let { mutableList.add(it) }

                ElementType.RELATION -> mutableList.addAll(getAllWayInRelation(member.ref))
            }
        }
        return mutableList
    }
}

private const val PREF_SAC_SCALE_WITHOUT_RELATION = "qs_AddSacScale_without_relation"
