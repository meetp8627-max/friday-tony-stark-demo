package io.livekit.android.example.voiceassistant.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.CallEnd
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material.icons.outlined.PresentToAll
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.livekit.android.annotations.Beta
import io.livekit.android.compose.types.TrackReference
import io.livekit.android.compose.ui.audio.AudioBarVisualizer

private val DockShape = RoundedCornerShape(50)

/**
 * One control inside the glass dock. Inactive controls are just a quiet icon
 * on the glass; an active control gets its own faint lit circle. Pressing
 * compresses it slightly — calm, no bounce.
 */
@Composable
private fun DockButton(
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    activeFill: Color = Color.White.copy(alpha = 0.16f),
    content: @Composable (tint: Color) -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
        animationSpec = tween(120),
        label = "dockPress"
    )
    val fill by animateColorAsState(
        targetValue = if (active) activeFill else Color.Transparent,
        animationSpec = tween(220),
        label = "dockFill"
    )
    val tint by animateColorAsState(
        targetValue = if (active) Color.White else Color.White.copy(alpha = 0.72f),
        animationSpec = tween(220),
        label = "dockTint"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(46.dp)
            .widthIn(min = 46.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(DockShape)
            .background(fill)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 11.dp)
    ) {
        content(tint)
    }
}

@Composable
private fun DockIcon(icon: ImageVector, description: String, tint: Color) {
    Icon(icon, contentDescription = description, tint = tint)
}

@OptIn(Beta::class)
@Composable
fun ControlBar(
    isMicEnabled: Boolean,
    onMicClick: () -> Unit,
    localAudioTrack: TrackReference?,
    isCameraEnabled: Boolean,
    onCameraClick: () -> Unit,
    isScreenShareEnabled: Boolean,
    onScreenShareClick: () -> Unit,
    isChatEnabled: Boolean,
    onChatClick: () -> Unit,
    onExitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // One continuous glass surface: dark translucent body, a faint sheen on
    // top, a hairline edge that is brighter at the top, and a soft shadow.
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .shadow(
                elevation = 20.dp,
                shape = DockShape,
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .clip(DockShape)
            .background(Color(0xA60C0B12))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.09f),
                        Color.White.copy(alpha = 0.0f)
                    )
                )
            )
            .border(
                width = 0.75.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.22f),
                        Color.White.copy(alpha = 0.04f)
                    )
                ),
                shape = DockShape
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        DockButton(active = isMicEnabled, onClick = onMicClick) { tint ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                DockIcon(
                    if (isMicEnabled) Icons.Outlined.Mic else Icons.Outlined.MicOff,
                    "Toggle Microphone",
                    tint
                )
                AnimatedVisibility(isMicEnabled) {
                    AudioBarVisualizer(
                        audioTrackRef = localAudioTrack,
                        brush = SolidColor(Color.White.copy(alpha = 0.85f)),
                        barCount = 3,
                        barWidth = 2.dp,
                        minHeight = 0.2f,
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .width(12.dp)
                            .height(20.dp)
                    )
                }
            }
        }

        DockButton(active = isCameraEnabled, onClick = onCameraClick) { tint ->
            DockIcon(
                if (isCameraEnabled) Icons.Outlined.Videocam else Icons.Outlined.VideocamOff,
                "Toggle Camera",
                tint
            )
        }

        DockButton(active = isScreenShareEnabled, onClick = onScreenShareClick) { tint ->
            DockIcon(Icons.Outlined.PresentToAll, "Toggle Screenshare", tint)
        }

        DockButton(active = isChatEnabled, onClick = onChatClick) { tint ->
            DockIcon(Icons.AutoMirrored.Outlined.Chat, "Toggle Chat", tint)
        }

        // End call: always lit, but in a restrained red glass rather than a
        // saturated icon — it should read as serious, not decorative.
        DockButton(
            active = true,
            onClick = onExitClick,
            activeFill = Color(0xFFE5484D).copy(alpha = 0.78f)
        ) { _ ->
            DockIcon(Icons.Outlined.CallEnd, "End Call", Color.White)
        }
    }
}

@Preview
@Composable
fun ControlBarPreview() {
    ControlBar(
        isMicEnabled = false,
        onMicClick = {},
        localAudioTrack = null,
        isCameraEnabled = false,
        onCameraClick = {},
        isScreenShareEnabled = false,
        onScreenShareClick = { },
        isChatEnabled = false,
        onChatClick = {},
        onExitClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
    )
}
