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
 * A calm, atmospheric palette — muted amber/gold and soft blue-violet, closer to
 * how light actually diffuses through frosted glass than a saturated rainbow.
 * Intentionally desaturated: color here should read as ambient, not decorative.
 */
private val SiriAmber = Color(0xFFE8B478)
private val SiriRose = Color(0xFFD98C9E)
private val SiriViolet = Color(0xFF8E8FD9)
private val SiriTeal = Color(0xFF7FB8C4)

/**
 * A soft, breathing glow blob — the visual centerpiece when MPro is idle,
 * listening, thinking, or speaking. [intensity] (0f..1f) drives how alive it
 * looks; drive it from agent state (listening/speaking = high, idle = low).
 * Built as one continuous soft field rather than a hard-edged ring, so it
 * reads as ambient light rather than a static decoration.
 */
@Composable
fun SiriGlowOrb(
    intensity: Float,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 220.dp,
) {
    val infinite = rememberInfiniteTransition(label = "siriGlow")

    // Slow, calm drift even at rest — never fully static, but never frantic.
    val rotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (9000 - (intensity * 5000)).toInt().coerceAtLeast(2200), easing = LinearEasing),
        ),
        label = "rotation"
    )

    val breathing by infinite.animateFloat(
        initialValue = 0.95f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathing"
    )

    val scale = breathing + (intensity * 0.08f)

    val atmosphere = Brush.sweepGradient(
        listOf(SiriAmber, SiriRose, SiriViolet, SiriTeal, SiriAmber)
    )

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        // Wide, very soft diffuse field — heavy blur relative to its size so
        // there's no crisp ring silhouette, just ambient color bleeding out.
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    rotationZ = rotation
                    alpha = 0.32f + (intensity * 0.18f)
                }
                .blur(72.dp)
                .clip(RoundedCornerShape(50))
                .background(atmosphere)
        )

        // A tighter inner field, blurred just enough to stay soft — this is
        // what gives the blob some internal depth instead of one flat wash.
        Box(
            modifier = Modifier
                .size(size * 0.62f)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    rotationZ = -rotation * 0.5f
                    alpha = 0.5f + (intensity * 0.2f)
                }
                .blur(36.dp)
                .clip(RoundedCornerShape(50))
                .background(atmosphere)
        )

        // Translucent glass core — lets a hint of the glow underneath show
        // through rather than fully occluding it with a flat opaque circle,
        // plus a soft specular highlight stacked on the same element.
        Box(
            modifier = Modifier
                .size(size * 0.46f)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.88f))
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color.White.copy(alpha = 0.10f),
                            Color.White.copy(alpha = 0.0f)
                        ),
                        radius = 220f
                    )
                )
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
