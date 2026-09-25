package de.westnordost.streetcomplete.overlays.restriction

import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_restriction_give_way
import de.westnordost.streetcomplete.resources.ic_restriction_stop
import de.westnordost.streetcomplete.resources.restriction_overlay_sign_give_way
import de.westnordost.streetcomplete.resources.restriction_overlay_sign_stop
import de.westnordost.streetcomplete.resources.restriction_overlay_sign_stop_all_way
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val RestrictionNodeType.icon: DrawableResource get() = when (this) {
    RestrictionNodeType.GIVE_WAY -> Res.drawable.ic_restriction_give_way
    RestrictionNodeType.STOP,
    RestrictionNodeType.ALL_WAY_STOP -> Res.drawable.ic_restriction_stop
}

val RestrictionNodeType.title: StringResource get() = when (this) {
    RestrictionNodeType.GIVE_WAY -> Res.string.restriction_overlay_sign_give_way
    RestrictionNodeType.STOP -> Res.string.restriction_overlay_sign_stop
    RestrictionNodeType.ALL_WAY_STOP -> Res.string.restriction_overlay_sign_stop_all_way
}
