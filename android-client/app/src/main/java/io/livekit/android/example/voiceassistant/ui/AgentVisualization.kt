package io.livekit.android.example.voiceassistant.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.livekit.android.annotations.Beta
import io.livekit.android.compose.state.Agent
import io.livekit.android.compose.ui.ScaleType
import io.livekit.android.compose.ui.VideoTrackView
import io.livekit.android.example.voiceassistant.ui.anim.CircleReveal

val revealSpringSpec = spring<Float>(stiffness = Spring.StiffnessVeryLow)
val hideSpringSpec = spring<Float>(stiffness = Spring.StiffnessMedium)

@OptIn(Beta::class)
@Composable
fun AgentVisualization(
    agent: Agent,
    modifier: Modifier = Modifier
) {

    val videoTrack = agent.videoTrack

    var hasFirstFrameRendered by remember(videoTrack) { mutableStateOf(false) }
    val revealed = videoTrack != null && hasFirstFrameRendered

    Box(modifier = modifier) {
        if (videoTrack != null) {
            VideoTrackView(
                trackReference = videoTrack,
                scaleType = ScaleType.FitInside,
                onFirstFrameRendered = { hasFirstFrameRendered = true },
                modifier = Modifier
                    .fillMaxSize(),
            )
        }
        CircleReveal(
            revealed = revealed,
            content = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    val intensity = siriIntensity(agent.agentState)
                    val label = siriStateLabel(agent.agentState, agent.isConnected)

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        SiriGlowOrb(
                            intensity = intensity,
                            modifier = Modifier.padding(bottom = 28.dp)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = label,
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Medium,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                            )
                            BlinkingCursor(
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }

                        val waitingAlpha by animateFloatAsState(
                            targetValue = if (agent.isConnected) 0f else 1f,
                            label = "waitingAlpha"
                        )
                        Text(
                            text = "Waiting for agent",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier
                                .padding(top = 12.dp)
                                .alpha(waitingAlpha)
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
            animationSpec = if (revealed) revealSpringSpec else hideSpringSpec,
        )
    }
}