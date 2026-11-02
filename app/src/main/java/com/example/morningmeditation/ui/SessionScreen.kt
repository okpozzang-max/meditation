package com.example.morningmeditation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.morningmeditation.domain.SessionState

@Composable
fun SessionScreen(
    state: SessionState,
    onPauseResume: () -> Unit,
    onSoundToggle: (Boolean) -> Unit,
    onFinish: () -> Unit,
) {
    val isPaused = state is SessionState.Paused
    val seconds = (state.remainingMillis + 999L) / 1_000L
    val timerText = "%02d:%02d".format(seconds / 60L, seconds % 60L)

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 28.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = timerText,
            fontFamily = FontFamily.Monospace,
            fontSize = 68.sp,
            lineHeight = 76.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = if (state.soundscape == com.example.morningmeditation.domain.Soundscape.NONE) {
                "소리 없이"
            } else {
                "${state.soundscape.label} 소리"
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(52.dp))
        Button(
            onClick = onPauseResume,
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            Text(if (isPaused) "계속하기" else "일시정지", fontSize = 17.sp)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { onSoundToggle(!state.soundEnabled) },
            enabled = state.soundscape != com.example.morningmeditation.domain.Soundscape.NONE,
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            Text(if (state.soundEnabled) "소리 끄기" else "소리 켜기", fontSize = 16.sp)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            Text("명상 마치기", fontSize = 16.sp)
        }
    }
}
