package de.westnordost.streetcomplete.quests.valves

import de.westnordost.streetcomplete.quests.valves.Valves.DUNLOP
import de.westnordost.streetcomplete.quests.valves.Valves.REGINA
import de.westnordost.streetcomplete.quests.valves.Valves.SCHRADER
import de.westnordost.streetcomplete.quests.valves.Valves.SCLAVERAND
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

enum class Valves(val osmValue: String) {
    SCHRADER("schrader"),
    SCLAVERAND("sclaverand"),
    DUNLOP("dunlop"),
    REGINA("regina");
}

val Valves.titleRes: StringResource get() = when (this) {
    SCHRADER ->     Res.string.quest_valves_schrader
    SCLAVERAND ->   Res.string.quest_valves_sclaverand
    DUNLOP ->       Res.string.quest_valves_dunlop
    REGINA ->       Res.string.quest_valves_regina
}

val Valves.iconRes: DrawableResource get() = when (this) {
    SCHRADER ->     Res.drawable.valves_schrader
    SCLAVERAND ->   Res.drawable.valves_presta
    DUNLOP ->       Res.drawable.valves_dunlop
    REGINA ->       Res.drawable.valves_regina
}
