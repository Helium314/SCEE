package de.westnordost.streetcomplete.quests.post_office

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.RadioGroupQuestForm
import org.jetbrains.compose.resources.stringResource

class AddPostOfficeType : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes, ways with
          amenity = post_office
          and !post_office
    """
    override val changesetComment = "Add post office"
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee
    override val wikiLink = "Key:post_office"
    override val icon = Res.drawable.ic_quest_post_office
    override val title = Res.string.quest_postOffice_title
    override val achievements = listOf(EditTypeAchievement.CITIZEN)

    override val hint = Res.string.quest_postOffice_postPartner_hint

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes with amenity = post_office or post_office")

    @Composable
    override fun Form(on: (QuestAction<String>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        RadioGroupQuestForm(
            on,
            listOf("bureau", "post_annex", "post_partner"),
            { stringResource(when (it) {
                "bureau" -> Res.string.quest_postOffice_bureau
                "post_annex" -> Res.string.quest_postOffice_postAnnex
                "post_partner" -> Res.string.quest_postOffice_postPartner
                else -> null
            }!!) }
        )
    }

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["post_office"] = answer
    }
}
