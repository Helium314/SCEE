package de.westnordost.streetcomplete.overlays.places

import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.asIfItWasnt
import de.westnordost.streetcomplete.osm.isAbandonedPlace
import de.westnordost.streetcomplete.osm.isDisusedPlace
import de.westnordost.streetcomplete.osm.isKindOfPlace
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PointStyle
import de.westnordost.streetcomplete.overlays.PolygonStyle
import de.westnordost.streetcomplete.quests.place_name.AddPlaceName
import de.westnordost.streetcomplete.quests.shop_type.CheckShopType
import de.westnordost.streetcomplete.quests.shop_type.SpecifyShopType
import de.westnordost.streetcomplete.util.getNameLabel
import de.westnordost.streetcomplete.view.presetIconIndex

class PlacesOverlay(private val getFeature: (Element) -> Feature?) : Overlay {

    override val title = R.string.overlay_places
    override val icon = R.drawable.ic_quest_shop
    override val changesetComment = "Survey shops, places etc."
    override val wikiLink = "StreetComplete/Overlays#Places"
    override val achievements = listOf(EditTypeAchievement.CITIZEN)
    override val hidesQuestTypes = setOf(
        AddPlaceName::class.simpleName!!,
        SpecifyShopType::class.simpleName!!,
        CheckShopType::class.simpleName!!
    )
    override val isCreateNodeEnabled = true

    override fun getStyledElements(mapData: MapDataWithGeometry) =
        mapData
            .asSequence()
            .filter { it.isKindOfPlace() }
            .mapNotNull { element ->
                val feature = getFeature(element)
                    ?: element.asIfItWasnt("disused")?.let { getFeature(it) }
                    ?: element.asIfItWasnt("abandoned")?.let { getFeature(it) }
                    ?: return@mapNotNull null

                val icon = feature.icon?.let { presetIconIndex[it] } ?: R.drawable.preset_maki_shop

                val label = getNameLabel(element.tags)

                val style = if (element is Node) {
                    PointStyle(icon, label,
                        if (element.isDisusedPlace()) "#606269"
                        else if (element.isAbandonedPlace()) "#B06269"
                        else null,
                        if (element.isDisusedPlace()) "#aaaaaf"
                        else if (element.isAbandonedPlace()) "#ffaaaa"
                        else null)
                } else {
                    PolygonStyle(Color.INVISIBLE, icon, label)
                }
                element to style
            } +
        // additionally show entrances but no addresses as they are already shown on the background
        mapData
            .filter("""
                nodes with
                  entrance
                  and !(addr:housenumber or addr:housename or addr:conscriptionnumber or addr:streetnumber)
            """)
            .map { it to PointStyle(icon = null, label = "◽") }

    override fun createForm(element: Element?) =
        // this check is necessary because the form shall not be shown for entrances
        if (element == null || element.isKindOfPlace()) PlacesOverlayForm() else null
}
