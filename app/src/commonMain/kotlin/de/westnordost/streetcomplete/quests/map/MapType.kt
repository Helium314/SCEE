package de.westnordost.streetcomplete.quests.map

import de.westnordost.streetcomplete.quests.map.MapType.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_mapType_scheme_description
import de.westnordost.streetcomplete.resources.quest_mapType_scheme_title
import de.westnordost.streetcomplete.resources.quest_mapType_street_description
import de.westnordost.streetcomplete.resources.quest_mapType_street_title
import de.westnordost.streetcomplete.resources.quest_mapType_topo_description
import de.westnordost.streetcomplete.resources.quest_mapType_topo_title
import de.westnordost.streetcomplete.resources.quest_mapType_toposcope_description
import de.westnordost.streetcomplete.resources.quest_mapType_toposcope_title
import de.westnordost.streetcomplete.resources.map_type_topo
import de.westnordost.streetcomplete.resources.map_type_street
import de.westnordost.streetcomplete.resources.map_type_scheme
import de.westnordost.streetcomplete.resources.map_type_toposcope
import org.jetbrains.compose.resources.StringResource

enum class MapType(val osmValue: String) {
    TOPO("topo"),
    STREET("street"),
    SCHEME("scheme"),
    TOPOSCOPE("toposcope")
}

val MapType.icon get() = when (this) {
    TOPO -> Res.drawable.map_type_topo
    STREET -> Res.drawable.map_type_street
    SCHEME -> Res.drawable.map_type_scheme
    TOPOSCOPE -> Res.drawable.map_type_toposcope
}

val MapType.title get() = when (this) {
    TOPO -> Res.string.quest_mapType_topo_title
    STREET -> Res.string.quest_mapType_street_title
    SCHEME -> Res.string.quest_mapType_scheme_title
    TOPOSCOPE -> Res.string.quest_mapType_toposcope_title
}

val MapType.description: StringResource
    get() = when (this) {
    TOPO -> Res.string.quest_mapType_topo_description
    STREET -> Res.string.quest_mapType_street_description
    SCHEME -> Res.string.quest_mapType_scheme_description
    TOPOSCOPE -> Res.string.quest_mapType_toposcope_description
}

