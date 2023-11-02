package de.westnordost.streetcomplete.quests.map

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.TextItem

class AddMapTypeForm : AListQuestForm<String>() {
    override val items = listOf(
        TextItem("topo", R.string.quest_mapType_topo),
        TextItem("street", R.string.quest_mapType_street),
        TextItem("scheme", R.string.quest_mapType_scheme),
        TextItem("toposcope", R.string.quest_mapType_toposcope),
    )
}
