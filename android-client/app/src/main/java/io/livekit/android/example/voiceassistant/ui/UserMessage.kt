package io.livekit.android.example.voiceassistant.ui


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.livekit.android.compose.types.ReceivedMessage

/**
 * Composable for displaying an individual user message.
 */
@Composable
fun UserMessage(
    message: ReceivedMessage,
    modifier: Modifier = Modifier
) {
    val state = remember {
        MutableTransitionState(false).apply {
            // Start the animation immediately.
            targetState = true
        }
    }
    AnimatedVisibility(
        visibleState = state,
        enter = fadeIn(androidx.compose.animation.core.tween(350)),
        modifier = modifier
    ) {
        // Your own words: small and quiet, no bubble — they belong to the
        // assistant environment rather than sitting in a chat box.
        Text(
            text = message.message,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.58f),
            modifier = modifier.padding(start = 56.dp)
        )
    }
}
