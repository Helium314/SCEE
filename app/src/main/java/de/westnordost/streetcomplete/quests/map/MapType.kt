package de.westnordost.streetcomplete.quests.map

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.map.MapType.*
import de.westnordost.streetcomplete.view.image_select.GroupableDisplayItem
import de.westnordost.streetcomplete.view.image_select.Item

enum class MapType(val osmValue: String) {
    ZERO("0"),
    ONE("1"),
    TWO("2"),
    THREE("3")
}
fun Collection<MapType>.toItems() = map { it.asItem() }

fun MapType.asItem(): GroupableDisplayItem<MapType> {
    return Item(this, imageResId, titleResId, descriptionResId)
}

private val MapType.imageResId: Int get() = when (this) {
    ZERO -> R.drawable.map_type_0
    ONE -> R.drawable.map_type_1
    TWO -> R.drawable.map_type_2
    THREE -> R.drawable.map_type_3
}

private val MapType.titleResId: Int get() = when (this) {
    ZERO -> R.string.quest_mapType_topo_title
    ONE -> R.string.quest_mapType_street_title
    TWO -> R.string.quest_mapType_scheme_title
    THREE -> R.string.quest_mapType_toposcope_title
}

private val MapType.descriptionResId: Int? get() = when (this) {
    ZERO -> R.string.quest_mapType_topo_description
    ONE -> R.string.quest_mapType_street_description
    TWO -> R.string.quest_mapType_scheme_description
    THREE -> R.string.quest_mapType_toposcope_description
    else -> null
}
