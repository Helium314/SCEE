package de.westnordost.streetcomplete.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Floating OK (check) button with animated pop-in/pop-out*/
@Composable
fun FloatingSmallerButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = scaleIn(tween(100), initialScale = 0.5f) + fadeIn(tween(100)),
        exit = scaleOut(tween(100), targetScale = 0.5f) + fadeOut(tween(100)),
    ) {
        FloatingActionButton(
            onClick = onClick,
            enabled = enabled,
            fabSize = 40.dp,
            content = content
        )
    }
}
