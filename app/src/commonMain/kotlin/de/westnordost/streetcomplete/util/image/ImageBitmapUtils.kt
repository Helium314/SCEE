package de.westnordost.streetcomplete.util.image

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import de.westnordost.streetcomplete.util.ktx.dpToPx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.readByteArray
import org.jetbrains.compose.resources.decodeToImageBitmap

@Composable
fun fileBitmapPainter(fileSystem: FileSystem, file: Path): Painter? {
    val imageBitmap by produceState<ImageBitmap?>(initialValue = null, key1 = file) {
        value = withContext(Dispatchers.IO) { fileSystem.loadImageBitmap(file) }
    }
    return remember(imageBitmap) { imageBitmap?.let { BitmapPainter(it) } }
}

fun FileSystem.loadImageBitmap(path: Path): ImageBitmap? = try {
    if (exists(path)) {
        source(path).buffered().use { it.readByteArray() }.decodeToImageBitmap()
    } else {
        null
    }
} catch (e: Exception) {
    null
}

/** allows creating a painter from all kinds of drawable resources,
 * though fixes the size which may waste memory or look blurry */
@Composable
fun compatPainterResource(@DrawableRes resId: Int, sizeDp: Int = 130): Painter {
    val ctx = LocalContext.current
    val drawable = ContextCompat.getDrawable(ctx, resId)
    return if (drawable is VectorDrawable || drawable is BitmapDrawable)
        painterResource(resId)
    else {
        val px = LocalResources.current.dpToPx(sizeDp).toInt()
        BitmapPainter(drawable!!.toBitmap(px, px).asImageBitmap())
    }
}

@Composable
fun Drawable.toPainter(sizeDp: Int = 130): Painter {
    val px = LocalResources.current.dpToPx(sizeDp).toInt()
    return BitmapPainter(toBitmap(px, px).asImageBitmap())
}
