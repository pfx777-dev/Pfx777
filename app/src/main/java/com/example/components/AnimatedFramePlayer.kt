package com.example.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.types.StudioAspectRatio
import com.example.ui.theme.CyanSpark
import com.example.ui.theme.IndigoElectric
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioObsidian
import com.example.ui.theme.StudioSurfaceElevated
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun AnimatedFramePlayer(
    framePaths: List<String>,
    posterPath: String,
    durationSeconds: Int,
    aspectRatio: StudioAspectRatio,
    modifier: Modifier = Modifier
) {
    val activePaths = remember(framePaths, posterPath) {
        if (framePaths.isNotEmpty()) framePaths else listOf(posterPath)
    }
    var isPlaying by remember(activePaths) { mutableStateOf(true) }
    var currentFrameIndex by remember(activePaths) { mutableIntStateOf(0) }

    LaunchedEffect(isPlaying, activePaths) {
        if (isPlaying && activePaths.size > 1) {
            val frameDelayMs = ((durationSeconds * 1000L) / activePaths.size).coerceIn(110L, 450L)
            while (true) {
                delay(frameDelayMs)
                currentFrameIndex = (currentFrameIndex + 1) % activePaths.size
            }
        }
    }

    val currentPath = activePaths.getOrElse(currentFrameIndex) { posterPath }
    val ratioFloat = (aspectRatio.width.toFloat() / aspectRatio.height.toFloat()).coerceIn(0.55f, 1.85f)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(ratioFloat)
                .clip(RoundedCornerShape(16.dp))
                .background(StudioObsidian)
        ) {
            AsyncImage(
                model = File(currentPath),
                contentDescription = "Animated video preview frame",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = StudioObsidian.copy(alpha = 0.78f),
                border = BorderStroke(1.dp, CyanSpark.copy(alpha = 0.45f)),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
            ) {
                Text(
                    text = "Frame ${currentFrameIndex + 1}/${activePaths.size} • ${durationSeconds}s",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanSpark,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = StudioSurfaceElevated,
            border = BorderStroke(1.dp, StudioCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .background(IndigoElectric.copy(alpha = 0.25f), CircleShape)
                        .testTag("video_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause preview" else "Play preview",
                        tint = CyanSpark
                    )
                }

                Slider(
                    value = currentFrameIndex.toFloat(),
                    onValueChange = {
                        isPlaying = false
                        currentFrameIndex = it.toInt().coerceIn(0, (activePaths.size - 1).coerceAtLeast(0))
                    },
                    valueRange = 0f..(activePaths.size - 1).coerceAtLeast(1).toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = CyanSpark,
                        activeTrackColor = IndigoElectric,
                        inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
