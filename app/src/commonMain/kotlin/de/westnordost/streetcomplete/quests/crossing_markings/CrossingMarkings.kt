package de.westnordost.streetcomplete.quests.crossing_markings

import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

enum class CrossingMarkings(
    val osmValue: String,
    val imageRes: DrawableResource?,
    val titleRes: StringResource?,
) {
    YES(
        osmValue = "yes",
        imageRes = null,
        titleRes = null
    ),
    NO(
        osmValue = "no",
        imageRes = Res.drawable.crossing_markings_no,
        titleRes = Res.string.quest_crossing_marking_value_no
    ),
    ZEBRA(
        osmValue = "zebra",
        imageRes = Res.drawable.crossing_markings_zebra,
        titleRes = Res.string.quest_crossing_marking_value_zebra
    ),
    LINES(
        osmValue = "lines",
        imageRes = Res.drawable.crossing_markings_lines,
        titleRes = Res.string.quest_crossing_marking_value_lines
    ),
    LADDER(
        osmValue = "ladder",
        imageRes = Res.drawable.crossing_markings_ladder,
        titleRes = Res.string.quest_crossing_marking_value_ladder
    ),
    DASHES(
        osmValue = "dashes",
        imageRes = Res.drawable.crossing_markings_dashes,
        titleRes = Res.string.quest_crossing_marking_value_dashes
    ),
    DOTS(
        osmValue = "dots",
        imageRes = Res.drawable.crossing_markings_dots,
        titleRes = Res.string.quest_crossing_marking_value_dots
    ),
    SURFACE(
        osmValue = "surface",
        imageRes = Res.drawable.crossing_markings_surface,
        titleRes = Res.string.quest_crossing_marking_value_surface
    ),
    LADDER_SKEWED(
        osmValue = "ladder:skewed",
        imageRes = Res.drawable.crossing_markings_ladder_skewed,
        titleRes = Res.string.quest_crossing_marking_value_ladder_skewed
    ),
    ZEBRA_PAIRED(
        osmValue = "zebra:paired",
        imageRes = Res.drawable.crossing_markings_zebra_paired,
        titleRes = Res.string.quest_crossing_marking_value_zebra_paired
    ),
    ZEBRA_BICOLOUR(
        osmValue = "zebra:bicolour",
        imageRes = Res.drawable.crossing_markings_zebra_bicolour,
        titleRes = Res.string.quest_crossing_marking_value_zebra_bicolour
    ),
    ZEBRA_DOUBLE(
        osmValue = "zebra:double",
        imageRes = Res.drawable.crossing_markings_zebra_double,
        titleRes = Res.string.quest_crossing_marking_value_zebra_double
    ),
    LADDER_PAIRED(
        osmValue = "ladder:paired",
        imageRes = Res.drawable.crossing_markings_ladder_paired,
        titleRes = Res.string.quest_crossing_marking_value_ladder_paired
    ),
    PICTOGRAMS(
        osmValue = "pictograms",
        imageRes = Res.drawable.crossing_markings_pictograms,
        titleRes = Res.string.quest_crossing_marking_value_pictograms
    )
}
