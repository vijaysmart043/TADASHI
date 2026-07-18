package com.vijay.tadashi.presentation.screens.home

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.vijay.tadashi.presentation.components.BasicButton
import com.vijay.tadashi.presentation.components.ScreenTitle
import com.vijay.tadashi.presentation.navigation.Screen
import com.vijay.tadashi.presentation.voice.VoiceEvents
import com.vijay.tadashi.presentation.voice.VoiceViewModel

@Composable
fun HomeScreen(
    navController: NavHostController,
    voiceViewModel: VoiceViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState = voiceViewModel.uiState
    val events = voiceViewModel.events

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        voiceViewModel.checkPermission()
        if (!isGranted) {
            Toast.makeText(context, "Microphone permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(key1 = Unit) {
        voiceViewModel.initializeTextToSpeech()
        voiceViewModel.checkPermission()
    }

    LaunchedEffect(key1 = events.value) {
        when (val event = events.value) {
            is VoiceEvents.RequestMicrophonePermission -> {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                voiceViewModel.onEventConsumed()
            }
            is VoiceEvents.ShowToast -> {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                voiceViewModel.onEventConsumed()
            }
            null -> {}
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
            onClick = {
                if (uiState.value.isListening) {
                    voiceViewModel.stopListening()
                } else {
                    voiceViewModel.startListening()
                }
            },
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(50)
        ) {
            Icon(
                imageVector = if (uiState.value.isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                contentDescription = if (uiState.value.isListening) "Stop" else "Start",
                modifier = Modifier.size(40.dp)
            )
        }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScreenTitle(title = "TADASHI")
            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.value.isListening) {
                Text(
                    text = "Listening...",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = uiState.value.recognizedText.ifEmpty { "Tap mic to start listening" },
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BasicButton(
                    text = "Speak Test",
                    onClick = {
                        voiceViewModel.speak(uiState.value.recognizedText.ifEmpty { "Hello from TADASHI!" })
                    },
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = { voiceViewModel.clearText() }) {
                    Text("Clear")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            BasicButton(
                text = "Go to Settings",
                onClick = {
                    navController.navigate(Screen.Settings.route)
                },
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}
