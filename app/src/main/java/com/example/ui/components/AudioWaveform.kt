package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.state.VoiceState
import com.example.ui.theme.WaveformBlue
import com.example.ui.theme.WaveformCyan
import com.example.ui.theme.WaveformMagenta
import com.example.ui.theme.WaveformPurple
import kotlin.math.sin

@Composable
fun AudioWaveform(
    voiceState: VoiceState,
    amplitude: Float = 0f,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    barCount: Int = 27
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")

    // Fluctuation phase for speaking / idle state
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    // Base heights distribution (bell curve with flank dots)
    val baseMultipliers = listOf(
        0.08f, 0.12f, 0.15f, 0.20f, 0.28f, 0.38f, 0.52f, 0.68f, 0.85f,
        0.95f, 1.0f, 0.95f, 0.88f, 0.76f, 0.64f, 0.55f, 0.70f, 0.82f,
        0.65f, 0.48f, 0.35f, 0.26f, 0.18f, 0.14f, 0.10f, 0.08f, 0.08f
    )

    Row(
        modifier = modifier
            .height(height)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val totalBars = minOf(barCount, baseMultipliers.size)
        for (i in 0 until totalBars) {
            val progress = i.toFloat() / totalBars.toFloat()
            val base = baseMultipliers[i]

            // Calculate height multiplier based on state
            val heightFraction = when (voiceState) {
                VoiceState.LISTENING -> {
                    val dynamicWave = sin(phase + i * 0.45f).toFloat() * 0.25f
                    (base * (0.3f + amplitude * 1.4f) + dynamicWave).coerceIn(0.08f, 1.0f)
                }
                VoiceState.SPEAKING -> {
                    val dynamicWave = sin(phase * 1.5f + i * 0.55f).toFloat() * 0.35f
                    (base * 0.85f + dynamicWave).coerceIn(0.12f, 1.0f)
                }
                VoiceState.PROCESSING -> {
                    val dynamicWave = (sin(phase * 2f + i * 0.7f).toFloat() * 0.2f).coerceAtLeast(0f)
                    (base * 0.35f + dynamicWave).coerceIn(0.08f, 0.7f)
                }
                VoiceState.IDLE, VoiceState.ERROR -> {
                    (base * 0.20f).coerceIn(0.06f, 0.30f)
                }
            }

            // Color gradient across the bars: Cyan -> Blue -> Purple -> Magenta
            val barColor = when {
                progress < 0.30f -> WaveformCyan
                progress < 0.60f -> WaveformBlue
                progress < 0.85f -> WaveformPurple
                else -> WaveformMagenta
            }

            Box(
                modifier = Modifier
                    .width(3.2.dp)
                    .fillMaxHeight(heightFraction)
                    .clip(if (heightFraction < 0.12f) CircleShape else RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                barColor.copy(alpha = 0.95f),
                                barColor.copy(alpha = 0.65f)
                            )
                        )
                    )
            )
        }
    }
}
