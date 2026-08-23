package de.westnordost.streetcomplete.quests.building_colour

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red

enum class BuildingColour(override val osmValue: String, override val androidValue: String?) :
    OsmColour {
    // Top used building colours
    WHITE("white", "#ffffff"),
    GREY80("#cccccc", null),
    BEIGEISH("#eecfaf", null),
    GREY("grey", "#808080"),
    BROWN("brown", "#a52a2a"),
    RED("red", "#ff0000"),
    YELLOW("yellow", "#ffff00"),
    BEIGE("beige", "#f5f5dc"),
    BLACK("black", "#000000"),
    GREEN("green", "#008000"),
    ORANGE("orange", "#ffa500"),
    BLUE("blue", "#0000ff"),
    POO("#85552e", null),
    LIGHT_GREY("lightgrey", "#d3d3d3"),
    SILVER("silver", "#c0c0c0"),
    TAN("tan", "#d2b48c"),
    YELLOWISH("#ffe0a0", null),
    LIGHT_YELLOW("lightyellow", "#ffffe0"),
    SLATE_GREY("#708090", null),
    REDDISH("#ff9e6b", null),

    // Rest of the recommended 3D palette
    MAROON("maroon", "#800000"),
    OLIVE("olive", "#808000"),
    TEAL("teal", "#008080"),
    NAVY("navy", "#000080"),
    PURPLE("purple", "#800080"),
    LIME("lime", "#00ff00"),
    AQUA("aqua", "#00ffff"),
    FUCHSIA("fuchsia", "#ff00ff"),
}

interface OsmColour {
    val androidValue: String?
    val osmValue: String
}

val OsmColour.title get() = this.osmValue

fun OsmColour.getDrawable(context: Context, iconResId: Int): Drawable {
    val color = Color.parseColor(this.androidValue ?: this.osmValue)
    val contrastColor = getBestContrast(context)
    val drawable = context.getDrawable(iconResId)!!.mutate()
    val matrix = ColorMatrix(
        floatArrayOf(
            color.red / 255f, 0f, contrastColor.red / 255f, 0f, 0f,
            color.green / 255f, 0f, contrastColor.green / 255f, 0f, 0f,
            color.blue / 255f, 0f, contrastColor.blue / 255f, 0f, 0f,
            1f, 1f, 1f, 1f, 0f
        )
    )
    drawable.colorFilter = ColorMatrixColorFilter(matrix)
    return drawable
}

private fun isDarkMode(context: Context): Boolean {
    val darkModeFlag = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return darkModeFlag == Configuration.UI_MODE_NIGHT_YES
}

private fun getBestContrast(context: Context): Int {
    return if (isDarkMode(context)) Color.LTGRAY else Color.DKGRAY
}
