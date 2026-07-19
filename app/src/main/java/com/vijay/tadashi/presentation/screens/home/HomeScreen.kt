package com.vijay.tadashi.presentation.screens.home

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.vijay.tadashi.presentation.chat.Sender
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
    val uiState by voiceViewModel.uiState.collectAsStateWithLifecycle()
    val events by voiceViewModel.events.collectAsStateWithLifecycle()

    val lazyListState = rememberLazyListState()
    val shouldScrollToEnd by remember { derivedStateOf { uiState.chatHistory.size } }

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

    LaunchedEffect(key1 = shouldScrollToEnd) {
        if (uiState.chatHistory.isNotEmpty()) {
            lazyListState.animateScrollToItem(uiState.chatHistory.lastIndex)
        }
    }

    LaunchedEffect(key1 = events) {
        when (val event = events) {
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
                    if (uiState.isListening) {
                        voiceViewModel.stopListening()
                    } else {
                        voiceViewModel.startListening()
                    }
                },
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(50)
            ) {
                Icon(
                    imageVector = if (uiState.isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                    contentDescription = if (uiState.isListening) "Stop" else "Start",
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                ScreenTitle(title = "TADASHI")
                if (uiState.isListening) {
                    Text(
                        text = "Listening...",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    state = lazyListState,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.chatHistory) { chatMessage ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = if (chatMessage.sender == Sender.ASSISTANT) 0.dp else 32.dp,
                                    end = if (chatMessage.sender == Sender.USER) 0.dp else 32.dp
                                ),
                            contentAlignment = if (chatMessage.sender == Sender.USER) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (chatMessage.sender == Sender.USER)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Text(
                                    text = chatMessage.text,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (chatMessage.sender == Sender.USER)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 104.dp, // Leave space for FAB
                        top = 16.dp,
                        bottom = 16.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = uiState.userInput,
                    onValueChange = { voiceViewModel.onUserInputChange(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text("Type your message...")
                    },
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        voiceViewModel.submitUserMessage(uiState.userInput)
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(28.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
