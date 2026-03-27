package de.westnordost.streetcomplete.screens.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlendModeColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.quest.Quest
import org.jetbrains.compose.resources.stringResource
import kotlin.collections.forEach

@Composable
fun NearbyQuestsView(
    nearbyQuests: Collection<Pair<Int, List<Quest>>>,
    modifier: Modifier,
    onSelectedQuest: (Quest) -> Unit
) {
    val scroll = rememberScrollState()
    LaunchedEffect(nearbyQuests) { scroll.scrollTo(0) }
    Column(
        modifier = modifier.heightIn(max = 330.dp).width(48.dp).verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        nearbyQuests.forEach { (color, quests) ->
            val imageFilter = BlendModeColorFilter(Color(ColorUtils.blendARGB(color, android.graphics.Color.WHITE, 0.8f)), BlendMode.Modulate)
            val circleColor = if (color == android.graphics.Color.WHITE) colorResource(R.color.quest_selection_frame) else Color(color)
            val boxModifier = Modifier.border(3.dp, circleColor, CircleShape)
            quests.forEach { quest ->
                Box(boxModifier) {
                    Image(
                        painter = painterResource(quest.type.icon),
                        contentDescription = stringResource(quest.type.title),
                        modifier = Modifier.size(48.dp).clickable { onSelectedQuest(quest) },
                        colorFilter = imageFilter
                    )
                }
            }
        }
    }
}
