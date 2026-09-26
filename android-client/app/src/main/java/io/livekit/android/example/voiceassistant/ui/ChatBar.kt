package io.livekit.android.example.voiceassistant.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension

// Same glow palette as the call-state orb and overlay bar — keeps the whole
// app's "new Siri" look consistent.
private val SiriPink = Color(0xFFFF3DAE)
private val SiriBlue = Color(0xFF3D7BFF)
private val SiriPurple = Color(0xFF9A4DFF)
private val SiriOrange = Color(0xFFFF9C3D)

@Composable
fun ChatBar(
    value: String,
    onValueChange: (String) -> Unit,
    onChatSend: (String) -> Unit,
    onMicClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val sendButtonColors = ButtonDefaults.buttonColors(
        containerColor = Color.Transparent,
        contentColor = Color.White
    )

    val infinite = rememberInfiniteTransition(label = "chatBarGlow")
    val rotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(7000, easing = LinearEasing)),
        label = "chatBarRotation"
    )
    // Desaturated / lower-alpha than the orb's glow — real ambient light bleed
    // reads as soft color, not a saturated rope wrapped around the shape.
    val ambientSweep = Brush.sweepGradient(
        listOf(
            SiriPink.copy(alpha = 0.55f),
            SiriBlue.copy(alpha = 0.55f),
            SiriPurple.copy(alpha = 0.55f),
            SiriOrange.copy(alpha = 0.55f),
            SiriPink.copy(alpha = 0.55f),
        )
    )

    Box(modifier = Modifier.then(modifier)) {
        // Soft, wide ambient glow bleeding outward — barely-there color, not a
        // crisp outline. This is what a light source behind frosted glass
        // actually looks like: diffuse, low-contrast, no hard edge.
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding((-14).dp)
                .graphicsLayer { rotationZ = rotation; alpha = 0.6f }
                .blur(32.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(ambientSweep)
        ) {}

        Box(
            modifier = Modifier
                .sizeIn(minHeight = 52.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(26.dp),
                    ambientColor = Color.Black.copy(alpha = 0.5f),
                    spotColor = Color.Black.copy(alpha = 0.5f),
                )
                .clip(RoundedCornerShape(26.dp))
                // Translucent tinted fill — lets whatever is behind it show
                // through faintly, which is what actually reads as "glass"
                // rather than a painted-on black bar.
                .background(Color(0xB3121016))
                // Thin bright edge that catches light at the top, fading
                // toward the bottom — the "this has an edge" cue real glass
                // always has.
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.06f),
                        )
                    ),
                    shape = RoundedCornerShape(26.dp),
                )
                .imePadding()
        ) {
            // Specular highlight — a soft diagonal sheen across the upper
            // portion, like light catching a curved glass surface.
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.10f),
                                Color.White.copy(alpha = 0.0f),
                            ),
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(0f, 140f),
                        )
                    )
            ) {}

        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            val (addButton, sendButton, messageInput) = createRefs()

            IconButton(
                onClick = { /* attachments — not wired yet */ },
                modifier = Modifier
                    .size(32.dp)
                    .constrainAs(addButton) {
                        start.linkTo(parent.start, 4.dp)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White.copy(alpha = 0.8f))
            }

            LKTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(color = Color.White),
                colors = TextFieldDefaults.colors().copy(
                    disabledTextColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                ),
                maxLines = 3,
                placeholder = {
                    Text("Ask MPro")
                },
                modifier = Modifier
                    .constrainAs(messageInput) {
                        start.linkTo(addButton.end, 4.dp)
                        end.linkTo(sendButton.start, 4.dp)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                        height = Dimension.wrapContent
                    },
            )

            Button(
                colors = sendButtonColors,
                shape = RoundedCornerShape(50),
                onClick = {
                    if (value.isNotEmpty()) onChatSend(value) else onMicClick?.invoke()
                },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .padding(0.dp)
                    .constrainAs(sendButton) {
                        end.linkTo(parent.end, 2.dp)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.preferredValue(36.dp)
                        height = Dimension.preferredValue(36.dp)
                    }
            ) {
                Icon(
                    if (value.isEmpty()) Icons.Default.Mic else Icons.Default.ArrowUpward,
                    contentDescription = if (value.isEmpty()) "Talk" else "Send Message",
                    tint = Color.White
                )
            }
        }
        }
    }
}

@Preview
@Composable
fun ChatWidgetPreview() {
    Column {
        var message by rememberSaveable { mutableStateOf("") }
        ChatBar(
            value = message,
            onValueChange = { message = it },
            onChatSend = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}