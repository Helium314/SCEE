package de.westnordost.streetcomplete.util

import android.content.Context
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.allDrawableResources
import org.jetbrains.compose.resources.DrawableResource

private fun drawableResId(name: String, context: Context): Int {
    nameToId[name]?.let { return it }
    val id = context.resources.getIdentifier(name, "drawable", context.packageName)
    require(id != 0) { "drawable $name not found"}
    nameToId[name] = id
    return id
}

// todo: ideally we should not need this...
fun DrawableResource.toResId(context: Context): Int = drawableResId(this.name, context)

val DrawableResource.name: String get() {
    drawableResourceToName[this]?.let { return it }
    val name = Res.allDrawableResources.entries.first { it.value == this }.key
    drawableResourceToName[this] = name
    return name
}

private val drawableResourceToName = hashMapOf<DrawableResource, String>()

private val nameToId = hashMapOf<String, Int>()
