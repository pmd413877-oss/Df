package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.state.VoiceState
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentCyanBright
import com.example.ui.theme.AccentMagenta
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryPurpleLight

@Composable
fun GlowingOrb(
    voiceState: VoiceState,
    amplitude: Float = 0f,
    size: Dp = 230.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_animation")

    // Slow breathing pulse for Idle
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_pulse"
    )

    // Fast rotation for processing
    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate_angle"
    )

    // Speaking pulse
    val speakingPulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speaking_pulse"
    )

    // Listening expansion reactive to mic amplitude
    val listeningScale = (1.0f + amplitude * 0.25f).coerceIn(1.0f, 1.30f)

    val currentScale = when (voiceState) {
        VoiceState.IDLE -> idlePulse
        VoiceState.LISTENING -> listeningScale
        VoiceState.PROCESSING -> 1.02f
        VoiceState.SPEAKING -> speakingPulse
        VoiceState.ERROR -> 1.0f
    }

    val primaryRingColor = when (voiceState) {
        VoiceState.IDLE -> AccentCyanBright
        VoiceState.LISTENING -> AccentCyanBright
        VoiceState.PROCESSING -> PrimaryPurpleLight
        VoiceState.SPEAKING -> AccentMagenta
        VoiceState.ERROR -> ErrorRed
    }

    val secondaryRingColor = when (voiceState) {
        VoiceState.IDLE -> PrimaryPurple
        VoiceState.LISTENING -> AccentCyan
        VoiceState.PROCESSING -> AccentCyanBright
        VoiceState.SPEAKING -> PrimaryPurpleLight
        VoiceState.ERROR -> Color(0xFF7F1D1D)
    }

    Box(
        modifier = modifier
            .size(size)
            .scale(currentScale)
            .testTag("glowing_orb_container"),
        contentAlignment = Alignment.Center
    ) {
        // Outer ambient glow ring
        Box(
            modifier = Modifier
                .size(size)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryRingColor.copy(alpha = 0.45f),
                                secondaryRingColor.copy(alpha = 0.20f),
                                Color.Transparent
                            )
                        )
                    )
                }
        )

        // Concentric Ring 1 (Outer neon orbit)
        Box(
            modifier = Modifier
                .size(size * 0.94f)
                .rotate(if (voiceState == VoiceState.PROCESSING) rotateAngle else 0f)
                .border(
                    width = 2.5.dp,
                    brush = Brush.sweepGradient(
                        listOf(
                            primaryRingColor,
                            secondaryRingColor,
                            primaryRingColor.copy(alpha = 0.3f),
                            secondaryRingColor,
                            primaryRingColor
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Concentric Ring 2 (Middle neon ring)
        Box(
            modifier = Modifier
                .size(size * 0.82f)
                .rotate(if (voiceState == VoiceState.PROCESSING) -rotateAngle else 45f)
                .border(
                    width = 1.8.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            primaryRingColor.copy(alpha = 0.8f),
                            secondaryRingColor.copy(alpha = 0.8f)
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Inner Avatar Circle with neon glow border
        Box(
            modifier = Modifier
                .size(size * 0.72f)
                .clip(CircleShape)
                .background(Color(0xFF0F1528))
                .border(
                    width = 3.dp,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryRingColor,
                            secondaryRingColor
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.zara_avatar),
                contentDescription = "Zara AI Avatar",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}
