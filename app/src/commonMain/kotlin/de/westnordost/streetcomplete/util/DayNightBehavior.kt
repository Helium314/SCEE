package de.westnordost.streetcomplete

import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.day_night_ignore
import de.westnordost.streetcomplete.resources.day_night_priority
import de.westnordost.streetcomplete.resources.day_night_visibility
import org.jetbrains.compose.resources.StringResource

enum class DayNightBehavior(val titleRes: StringResource) {
    IGNORE(Res.string.day_night_ignore),
    PRIORITY(Res.string.day_night_priority),
    VISIBILITY(Res.string.day_night_visibility)
}
