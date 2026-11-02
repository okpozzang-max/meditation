package com.example.morningmeditation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.morningmeditation.ui.theme.MorningMeditationTheme
import com.example.morningmeditation.domain.SessionState
import com.example.morningmeditation.ui.HomeScreen
import com.example.morningmeditation.ui.MeditationViewModel
import com.example.morningmeditation.ui.SessionScreen

class MainActivity : ComponentActivity() {
    private val viewModel: MeditationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        setContent {
            MorningMeditationTheme {
                val uiState by viewModel.uiState.collectAsState()
                when (val session = uiState.session.state) {
                    is SessionState.Running, is SessionState.Paused -> SessionScreen(
                        state = session,
                        onPauseResume = {
                            if (session is SessionState.Running) viewModel.pause() else viewModel.resume()
                        },
                        onSoundToggle = viewModel::setSoundEnabled,
                        onFinish = viewModel::finish,
                    )
                    else -> HomeScreen(
                        selection = uiState.home.selection,
                        onSelectionChanged = viewModel::saveSelection,
                        onStart = viewModel::start,
                    )
                }
            }
        }
    }
}
