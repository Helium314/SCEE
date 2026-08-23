package de.westnordost.streetcomplete.quests.toilets_disposal

import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.StringResource

enum class ToiletsDisposalType(val osmValue: String, val title: StringResource) {
    FLUSH("flush", Res.string.quest_toilets_disposal_flush),
    PIT_LATRINE("pitlatrine", Res.string.quest_toilets_disposal_pit),
    CHEMICAL("chemical", Res.string.quest_toilets_disposal_chemical),
}
