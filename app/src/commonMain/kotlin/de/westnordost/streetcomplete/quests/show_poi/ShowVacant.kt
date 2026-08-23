package de.westnordost.streetcomplete.quests.show_poi

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.places.applyReplacePlaceTo
import de.westnordost.streetcomplete.osm.places.isPlace
import de.westnordost.streetcomplete.osm.updateCheckDate
import de.westnordost.streetcomplete.quests.getLabelSources
import de.westnordost.streetcomplete.quests.shop_type.ShopType
import de.westnordost.streetcomplete.quests.shop_type.ShopTypeAnswer
import de.westnordost.streetcomplete.quests.LabelOrElementSelectionDialog
import de.westnordost.streetcomplete.quests.shop_type.ShopTypeQuestForm
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*

class ShowVacant : OsmFilterQuestType<ShopTypeAnswer>() {
    override val elementFilter = """
        nodes, ways, relations with
        shop = vacant
        or disused:shop
        or disused:amenity
        or disused:office
    """
    override val changesetComment = "Adjust vacant places"
    override val wikiLink = "Key:disused:"
    override val icon = Res.drawable.ic_quest_poi_vacant
    override val title = Res.string.quest_poi_vacant_title
    override val dotColor = "grey"
    override val defaultDisabledMessage = Res.string.default_disabled_msg_poi_vacant
    override val dotLabelSources = getLabelSources("label", this, prefs)

    @Composable
    override fun Form(on: (QuestAction<ShopTypeAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ShopTypeQuestForm(on, element, countryInfo)
    }

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.asSequence().filter { it.isPlace() }

    override fun applyAnswerTo(answer: ShopTypeAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is ShopTypeAnswer.IsShopVacant -> tags.updateCheckDate()
            is ShopType -> answer.feature.applyReplacePlaceTo(tags)
            is ShopTypeAnswer.LeaveNote -> {} // can this happen?
        }
    }

    @Composable
    override fun QuestSettings(onDismissRequest: () -> Unit) {
        LabelOrElementSelectionDialog(this, prefs, onDismissRequest)
    }
}
