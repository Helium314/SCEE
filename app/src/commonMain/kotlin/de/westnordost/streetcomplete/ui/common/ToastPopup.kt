package de.westnordost.streetcomplete.ui.common

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import de.westnordost.streetcomplete.ui.util.rememberScreenAlignmentPopupPositionProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun ToastPopup(
    onDismissRequest: () -> Unit,
    text: String,
    duration: Duration = 3.seconds,
    isInDialog: Boolean = false
) {
    val ctx = LocalContext.current
    var isVisible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (isVisible) 1f else 0f, tween(300))

     LaunchedEffect(text) {
         if (isInDialog) {
             // compose doesn't display that stuff on top of a dialog, so we fall back to the classic android toast
             withContext(Dispatchers.Main) { Toast.makeText(ctx, text, Toast.LENGTH_LONG).show() }
         } else {
             isVisible = true
             delay(duration)
             isVisible = false
             delay(300)
         }
         onDismissRequest()
    }

    val popupPositionProvider = rememberScreenAlignmentPopupPositionProvider(Alignment.BottomCenter)
    Popup(
        popupPositionProvider = popupPositionProvider,
        properties = PopupProperties(
            focusable = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            elevation = 4.dp,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .alpha(alpha)
                .padding(vertical = 48.dp, horizontal = 24.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
