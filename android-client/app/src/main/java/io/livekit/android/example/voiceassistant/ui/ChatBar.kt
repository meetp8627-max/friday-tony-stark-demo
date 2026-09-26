package io.livekit.android.example.voiceassistant.ui

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension

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

    val barShape = RoundedCornerShape(26.dp)

    Box(
        modifier = Modifier
            .then(modifier)
            .sizeIn(minHeight = 52.dp)
            .fillMaxWidth()
            .shadow(
                elevation = 18.dp,
                shape = barShape,
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = Color.Black.copy(alpha = 0.5f)
            )
            .clip(barShape)
            // Translucent tinted fill — lets whatever is behind it show
            // through faintly, which is what actually reads as "glass"
            // rather than a painted-on flat black bar.
            .background(Color(0xB3121016))
            // Thin bright edge that catches light at the top, fading
            // toward the bottom — the "this has an edge" cue real glass
            // always has.
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.06f)
                    )
                ),
                shape = barShape
            )
            .imePadding()
    ) {
        // Specular highlight — a soft sheen across the upper portion, like
        // light catching a curved glass surface.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(barShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.10f),
                            Color.White.copy(alpha = 0.0f)
                        )
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
