package de.westnordost.streetcomplete.quests.pharmacy

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.util.countryboundaries.AllCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.places.isPlace
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.util.ktx.toYesNo
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.YesNoQuestForm

class AddIsPharmacyDispensing : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes,ways with
        (
            amenity = pharmacy
            or healthcare = pharmacy
        )
        and (!dispensing or dispensing older today -8 years)
    """
    override val changesetComment = "Determine whether pharmacies are dispensing prescription drugs"
    override val wikiLink = "Key:dispensing"
    override val icon = Res.drawable.ic_quest_pharmacy
    override val title = Res.string.quest_is_pharmacy_dispensing_title
    override val achievements = listOf(CITIZEN)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_go_inside_regional_warning
    override val enabledInCountries = AllCountriesExcept("AT", "DE", "FR", "PL")

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.asSequence().filter { it.isPlace() }

    @Composable
    override fun Form(on: (QuestAction<Boolean>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        YesNoQuestForm(on)
    }

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("dispensing", answer.toYesNo())
    }
}
