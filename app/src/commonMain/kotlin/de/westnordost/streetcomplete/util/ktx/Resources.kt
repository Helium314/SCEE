package de.westnordost.streetcomplete.util.ktx

import android.content.res.Resources
import androidx.core.util.TypedValueCompat
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.allDrawableResources
import org.jetbrains.compose.resources.DrawableResource

/** return the number of pixels for the given density independent pixels */
fun Resources.dpToPx(dp: Number): Float = TypedValueCompat.dpToPx(dp.toFloat(), displayMetrics)

val DrawableResource.name: String get() {
    drawableResourceToName[this]?.let { return it }
    val name = Res.allDrawableResources.entries.first { it.value == this }.key
    drawableResourceToName[this] = name
    return name
}

private val drawableResourceToName = hashMapOf<DrawableResource, String>()
