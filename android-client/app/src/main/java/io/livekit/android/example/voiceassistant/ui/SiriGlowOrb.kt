package io.livekit.android.example.voiceassistant.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * The glow palette Apple has been teasing for the redesigned Siri coming in iOS 27:
 * a dark, Dynamic-Island-based interface with a rotating pink/blue/purple/orange glow
 * around its edges, most striking in dark mode.
 */
private val SiriPink = Color(0xFFFF3DAE)
private val SiriBlue = Color(0xFF3D7BFF)
private val SiriPurple = Color(0xFF9A4DFF)
private val SiriOrange = Color(0xFFFF9C3D)

/**
 * A pulsing, rotating glow "island" — the visual centerpiece when FRIDAY is idle,
 * listening, thinking, or speaking. [intensity] (0f..1f) drives how energetic the
 * glow looks; drive it from agent state (listening/speaking = high, idle = low).
 */
@Composable
fun SiriGlowOrb(
    intensity: Float,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 220.dp,
) {
    val infinite = rememberInfiniteTransition(label = "siriGlow")

    val rotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (4200 - (intensity * 2600)).toInt().coerceAtLeast(900), easing = LinearEasing),
        ),
        label = "rotation"
    )

    val breathing by infinite.animateFloat(
        initialValue = 0.93f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathing"
    )

    val scale = breathing + (intensity * 0.10f)

    val sweep = Brush.sweepGradient(
        listOf(SiriPink, SiriBlue, SiriPurple, SiriOrange, SiriPink)
    )

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        // Diffuse outer glow — the "light bleeding out" look
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    rotationZ = rotation
                    alpha = 0.55f + (intensity * 0.25f)
                }
                .blur(56.dp)
                .clip(RoundedCornerShape(50))
                .background(sweep)
        )

        // Tighter, brighter ring closer to the core
        Box(
            modifier = Modifier
                .size(size * 0.72f)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    rotationZ = -rotation * 0.6f
                }
                .blur(20.dp)
                .clip(RoundedCornerShape(50))
                .background(sweep)
        )

        // Dark "island" core — everything else glows around this
        Box(
            modifier = Modifier
                .size(size * 0.55f)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.background)
        )
    }
}

/** A thin blinking text-cursor, echoing the "Search or Ask" prompt cursor. */
@Composable
fun BlinkingCursor(color: Color, modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "cursorBlink")
    val alpha by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "cursorAlpha"
    )
    Box(
        modifier = modifier
            .width(2.dp)
            .height(18.dp)
            .background(color.copy(alpha = alpha))
    )
}

/**
 * Maps whatever the SDK's agent-state type is to a short label, via toString()
 * matching so this keeps compiling even if the exact enum member names differ
 * between SDK versions.
 */
fun <T> siriStateLabel(agentState: T?, isConnected: Boolean): String {
    if (!isConnected) return "Connecting"
    val raw = agentState?.toString()?.lowercase() ?: ""
    return when {
        "listen" in raw -> "Listening"
        "think" in raw -> "Thinking"
        "speak" in raw -> "Speaking"
        else -> "Search or Ask"
    }
}

/** Rough activity level (0f..1f) for the glow, based on the same state string. */
fun <T> siriIntensity(agentState: T?): Float {
    val raw = agentState?.toString()?.lowercase() ?: ""
    return when {
        "speak" in raw -> 1f
        "listen" in raw -> 0.75f
        "think" in raw -> 0.5f
        else -> 0.15f
    }
}
