package de.westnordost.streetcomplete.quests.roof_shape

import de.westnordost.streetcomplete.quests.roof_shape.RoofShape.*
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource

val RoofShape.icon: DrawableResource get() = when (this) {
    GABLED ->            Res.drawable.roof_gabled
    HIPPED ->            Res.drawable.roof_hipped
    FLAT ->              Res.drawable.roof_flat
    PYRAMIDAL ->         Res.drawable.roof_pyramidal
    HALF_HIPPED ->       Res.drawable.roof_half_hipped
    SKILLION ->          Res.drawable.roof_skillion
    GAMBREL ->           Res.drawable.roof_gambrel
    ROUND ->             Res.drawable.roof_round
    DOUBLE_SALTBOX ->    Res.drawable.roof_double_saltbox
    SALTBOX ->           Res.drawable.roof_saltbox
    MANSARD ->           Res.drawable.roof_mansard
    DOME ->              Res.drawable.roof_dome
    QUADRUPLE_SALTBOX -> Res.drawable.roof_quadruple_saltbox
    ROUND_GABLED ->      Res.drawable.roof_round_gabled
    ONION ->             Res.drawable.roof_onion
    CONE ->              Res.drawable.roof_cone
    SAWTOOTH ->          Res.drawable.ic_roof_sawtooth
    SIDE_HIPPED ->       Res.drawable.ic_roof_side_hipped
    SIDE_HALF_HIPPED ->  Res.drawable.ic_roof_side_half_hipped
    CROSSPITCHED ->      Res.drawable.ic_roof_crosspitched
    HIPPED_AND_GABLED -> Res.drawable.ic_roof_hip_and_gable
    GABLED_HEIGHT_MOVED -> Res.drawable.ic_roof_gabled_height_moved
    MANY ->              Res.drawable.empty_96
}
