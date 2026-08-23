package de.westnordost.streetcomplete.quests.lamp_mount

import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*

sealed interface LampMountAnswer

enum class Support(val mount: String) : LampMountAnswer {
    CEILING("ceiling"),
    TRANSIT_SHELTER("street_furniture:transit_shelter")
}

enum class LampMount(val mount: String) : LampMountAnswer {
    STRAIGHT_MAST("straight_mast"),
    BENT_MAST("bent_mast"),
    SUSPENDED("suspended"),
    ANGLED_MAST("angled_mast"),
    HIGH_MAST("high_mast"),
    BOLLARD("bollard"),
    WALL("wall"),
}

val LampMountAnswer.title get() = when (this) {
    is LampMount -> when (this) {
        LampMount.STRAIGHT_MAST -> Res.string.quest_lampMount_straightMast
        LampMount.BENT_MAST -> Res.string.quest_lampMount_bentMast
        LampMount.SUSPENDED -> Res.string.quest_lampMount_suspended
        LampMount.ANGLED_MAST -> Res.string.quest_lampMount_angledMast
        LampMount.HIGH_MAST -> Res.string.quest_lampMount_highMast
        LampMount.BOLLARD -> Res.string.quest_lampMount_bollard
        LampMount.WALL -> Res.string.quest_lampMount_wall
    }
    is Support -> when (this) {
        Support.CEILING -> Res.string.quest_lampMount_ceiling
        Support.TRANSIT_SHELTER -> Res.string.quest_lampMount_transitShelter
    }
}
