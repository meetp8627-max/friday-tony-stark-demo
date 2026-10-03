package io.livekit.android.example.voiceassistant.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.livekit.android.compose.types.ReceivedMessage
import io.livekit.android.room.Room

@Composable
fun ChatLog(room: Room, messages: List<ReceivedMessage>, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        // Get and display the transcriptions.
        val displayTranscriptions = messages.asReversed()
        val lazyListState = rememberLazyListState()

        // Scroll to bottom when new transcriptions come in.
        LaunchedEffect(messages.count()) {
            lazyListState.animateScrollToItem(0)
        }
        LazyColumn(
            userScrollEnabled = true,
            state = lazyListState,
            reverseLayout = true,
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .drawWithContent {
                    // Fade top
                    val colors = arrayOf(
                        0.0f to Color.Transparent,
                        0.15f to Color.Black,
                        1.0f to Color.Black,
                    )
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            colorStops = colors
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
        ) {
            itemsIndexed(
                items = displayTranscriptions,
                key = { _, transcription -> transcription.id },
            ) { index, message ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 10.dp)
                        .animateItem()
                ) {
                    if (message.fromParticipant?.identity == room.localParticipant.identity) {
                        UserMessage(
                            message = message,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    } else {
                        // Assistant response: large light text straight on the
                        // screen. The newest reads at full strength, older
                        // responses recede.
                        val strength by animateFloatAsState(
                            targetValue = if (index == 0) 0.96f else 0.45f,
                            animationSpec = tween(450),
                            label = "responseStrength"
                        )
                        Text(
                            text = message.message,
                            fontSize = 22.sp,
                            lineHeight = 30.sp,
                            fontWeight = FontWeight.Light,
                            color = Color.White.copy(alpha = strength),
                            modifier = Modifier.align(Alignment.CenterStart)
                        )
                    }
                }
            }
        }
    }
}