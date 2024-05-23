package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.getLastCheckDateKeys

val INVALID_SURFACES = setOf(
    "cobblestone", // https://wiki.openstreetmap.org/wiki/Tag%3Asurface%3Dcobblestone
    "cement", // https://community.openstreetmap.org/t/mysterious-surface-cement/5158 and https://wiki.openstreetmap.org/wiki/Tag:surface%3Dconcrete
)

val FULLY_PAVED_SURFACES = setOf(
    "paved",
    // sealed
    "asphalt", "concrete", "chipseal", "concrete:plates", "cement",
    // paving stones
    "brick", "bricks", "paving_stones", "paving_stones:30",
    "sett", "cobblestone", "cobblestone:flattened", "unhewn_cobblestone",
    // other
    "metal", "wood", "plastic", "acrylic", "tartan", "rubber",
)

val NATURAL_SURFACES = setOf(
    "ground",
    // earthy
    "earth", "dirt", "soil", "grass", "sand", "mud",
    // other
    "ice", "salt", "snow", "rock", "stone", "stepping_stones",
)

val UNPAVED_SURFACES = NATURAL_SURFACES + setOf(
    "unpaved",
    "compacted", "gravel", "fine_gravel", "pebblestone",
    "woodchips", "artificial_turf", "clay",
)

val PAVED_SURFACES = FULLY_PAVED_SURFACES + setOf(
    "concrete:lanes",
    "grass_paver",
    "metal_grid",
)

private val SOFT_SURFACES = setOf(
    "earth", "dirt", "soil", "grass", "sand", "mud", "snow", "woodchips", "artificial_turf"
)

private val UNPAVED_BUT_NOT_ALWAYS_SOFT = setOf(
    "ground", // see https://community.openstreetmap.org/t/is-tracktype-grade2-also-for-trails-with-large-naturally-occuring-pieces-of-rock/96850
    "unpaved", "compacted", "gravel", "fine_gravel", "pebblestone", "grass_paver"
)

val INVALID_SURFACES_FOR_TRACKTYPES = mapOf(
    "grade1" to ANYTHING_UNPAVED,
    "grade2" to SOFT_SURFACES,
    "grade3" to ANYTHING_FULLY_PAVED,
    "grade4" to ANYTHING_FULLY_PAVED,
    "grade5" to ANYTHING_FULLY_PAVED,
)

/** @return whether the given tag value for [surface] contradicts the tag value for [tracktype].
 *  E.g. surface=asphalt but tracktype=grade5. */
fun isSurfaceAndTracktypeConflicting(surface: String, tracktype: String?): Boolean =
    INVALID_SURFACES_FOR_TRACKTYPES[tracktype]?.contains(surface) == true

val EXPECTED_SURFACES_FOR_TRACKTYPES = mapOf(
    "grade1" to ANYTHING_FULLY_PAVED,
    "grade2" to UNPAVED_BUT_NOT_ALWAYS_SOFT,
    "grade3" to ANYTHING_UNPAVED,
    "grade4" to ANYTHING_UNPAVED,
    "grade5" to SOFT_SURFACES,
)

/** @return whether the given tag value for [surface] likely contradicts the tag value for [tracktype].
 *  E.g. surface=asphalt but tracktype=grade2.
 *  some such combinations may be actually valid, so should not be assumed to be always be wrong
 *  but if someone edits surface it is preferable to remove suspicious tracktype and trigger resurvey
 *  see https://github.com/streetcomplete/StreetComplete/issues/5236
 *  */
fun isSurfaceAndTracktypeCombinationSuspicious(surface: String, tracktype: String?): Boolean =
    tracktype != null && EXPECTED_SURFACES_FOR_TRACKTYPES[tracktype]?.contains(surface) != true

/** Sets the common surface of the foot- and cycleway parts into the surface tag, if any. If the
 *  surfaces of the foot- and cycleway parts have nothing in common, removes the surface tag */
fun updateCommonSurfaceFromFootAndCyclewaySurface(tags: Tags) {
    val footwaySurface = tags["footway:surface"]
    val cyclewaySurface = tags["cycleway:surface"]
    if (cyclewaySurface != null && footwaySurface != null) {
        val commonSurface = getCommonSurface(footwaySurface, cyclewaySurface)
        if (commonSurface != null) {
            SurfaceAndNote(parseSurface(commonSurface), tags["surface:note"]).applyTo(tags)
        } else {
            tags.remove("surface")
            tags.remove("surface:note")
            getKeysAssociatedWithSurface().forEach { tags.remove(it) }
        }
    }
}

private fun getCommonSurface(vararg surface: String?): String? = when {
    surface.any { it == null } -> null
    surface.all { it == surface.firstOrNull() } -> surface.firstOrNull()
    surface.all { it in PAVED_SURFACES } -> "paved"
    surface.all { it in UNPAVED_SURFACES } -> "unpaved"
    else -> null
}

fun getKeysAssociatedWithSurface(prefix: String = ""): Set<String> =
    setOf(
        "${prefix}surface:grade",
        "${prefix}surface:colour",
        "source:${prefix}surface",
        "${prefix}smoothness",
        "${prefix}smoothness:date",
        "source:${prefix}smoothness",
        "${prefix}paving_stones:shape",
        "${prefix}paving_stones:pattern",
        "${prefix}paving_stones:length",
        "${prefix}paving_stones:width",
    ) +
        getLastCheckDateKeys("${prefix}surface") +
        getLastCheckDateKeys("${prefix}smoothness")
