package de.westnordost.streetcomplete.quests.lamp_mount

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.TextItem

class AddLampMountForm : AListQuestForm<String>() {
    override val items = listOf(
        TextItem("straight_mast", R.string.quest_lampMount_straightMast),
        TextItem("bent_mast", R.string.quest_lampMount_bentMast),
        TextItem("suspended", R.string.quest_lampMount_suspended),
        TextItem("angled_mast", R.string.quest_lampMount_angledMast),
        TextItem("high_mast", R.string.quest_lampMount_highMast),
        TextItem("bollard", R.string.quest_lampMount_bollard),
        TextItem("wall", R.string.quest_lampMount_wall),
        TextItem("ceiling", R.string.quest_lampMount_ceiling),
        TextItem("street_furniture:transit_shelter", R.string.quest_lampMount_transitShelter),
    )
}
