package io.livekit.android.example.voiceassistant.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.livekit.android.annotations.Beta
import io.livekit.android.compose.state.Agent

/**
 * Compact status line — mirrors how real Siri conveys state: a small glowing
 * indicator near the top plus a short label, never a large shape occupying
 * the middle of the screen. The conversation transcript owns that space.
 */
@OptIn(Beta::class)
@Composable
fun AgentVisualization(
    agent: Agent,
    modifier: Modifier = Modifier
) {
    val intensity = siriIntensity(agent.agentState)
    val label = siriStateLabel(agent.agentState, agent.isConnected)

    androidx.compose.runtime.LaunchedEffect(label, intensity) {
        io.livekit.android.example.voiceassistant.overlay.OverlayState.update(label, intensity)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
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
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            modifier = Modifier
                .padding(top = 26.dp)
                .alpha(waitingAlpha)
        )
    }
}