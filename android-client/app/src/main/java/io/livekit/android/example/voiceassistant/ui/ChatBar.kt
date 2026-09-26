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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
        animationSpec = infiniteRepeatable(animation = tween(6000, easing = LinearEasing)),
        label = "chatBarRotation"
    )
    val sweep = Brush.sweepGradient(listOf(SiriPink, SiriBlue, SiriPurple, SiriOrange, SiriPink))

    Box(
        modifier = Modifier
            .imePadding()
            .sizeIn(minHeight = 52.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .clip(RoundedCornerShape(26.dp))
            .background(sweep)
            .padding(1.5.dp) // glow border thickness
            .clip(RoundedCornerShape(25.dp))
            .background(Color(0xB3121016)) // translucent glass fill
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.06f)
                    )
                ),
                shape = RoundedCornerShape(25.dp)
            )
            .then(modifier)
    ) {
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
                        width = Dimension.value(36.dp)
                        height = Dimension.value(36.dp)
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