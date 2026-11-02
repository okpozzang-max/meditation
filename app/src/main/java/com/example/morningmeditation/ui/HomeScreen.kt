package com.example.morningmeditation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.morningmeditation.data.SessionSelection
import com.example.morningmeditation.domain.MeditationDuration
import com.example.morningmeditation.domain.Soundscape

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    selection: SessionSelection,
    onStart: (MeditationDuration, Soundscape) -> Unit,
    onSelectionChanged: (SessionSelection) -> Unit = {},
) {
    var duration by remember(selection.duration) { mutableStateOf(selection.duration) }
    var soundscape by remember(selection.soundscape) { mutableStateOf(selection.soundscape) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "아침 명상",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "짧게 머물고, 천천히 시작해요",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )

        Spacer(Modifier.height(40.dp))
        Text("명상 시간", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
        Spacer(Modifier.height(12.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 5,
        ) {
            MeditationDuration.entries.forEach { option ->
                FilterChip(
                    selected = duration == option,
                    onClick = {
                        duration = option
                        onSelectionChanged(SessionSelection(duration, soundscape))
                    },
                    label = { Text("${option.minutes}분") },
                    modifier = Modifier.heightIn(min = 48.dp),
                )
            }
        }

        Spacer(Modifier.height(28.dp))
        Text("배경 소리", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
        Spacer(Modifier.height(12.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 3,
        ) {
            Soundscape.entries.forEach { option ->
                FilterChip(
                    selected = soundscape == option,
                    onClick = {
                        soundscape = option
                        onSelectionChanged(SessionSelection(duration, soundscape))
                    },
                    label = { Text(option.label) },
                    modifier = Modifier.heightIn(min = 48.dp),
                )
            }
        }

        Spacer(Modifier.height(36.dp))
        Button(
            onClick = { onStart(duration, soundscape) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            Text("명상 시작", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
    }
}
