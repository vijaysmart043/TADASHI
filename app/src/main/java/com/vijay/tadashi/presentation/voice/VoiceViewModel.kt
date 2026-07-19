package com.vijay.tadashi.presentation.voice

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vijay.tadashi.core.ai.AssistantEngine
import com.vijay.tadashi.core.voice.SpeechRecognizerManager
import com.vijay.tadashi.core.voice.TextToSpeechManager
import com.vijay.tadashi.presentation.chat.ChatMessage
import com.vijay.tadashi.presentation.chat.Sender
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoiceViewModel @Inject constructor(
    private val speechRecognizerManager: SpeechRecognizerManager,
    private val textToSpeechManager: TextToSpeechManager,
    private val assistantEngine: AssistantEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceUiState())
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<VoiceEvents?>(null)
    val events: StateFlow<VoiceEvents?> = _events.asStateFlow()

    init {
        setupSpeechRecognizerCallbacks()
    }

    private fun setupSpeechRecognizerCallbacks() {
        speechRecognizerManager.onListeningStarted = {
            Log.d("TADASHI-VOICE", "Listening started")
            _uiState.value = _uiState.value.copy(isListening = true)
        }
        speechRecognizerManager.onListeningStopped = {
            Log.d("TADASHI-VOICE", "Listening stopped")
            _uiState.value = _uiState.value.copy(isListening = false)
        }
        speechRecognizerManager.onResult = { text ->
            Log.d("TADASHI-VOICE", "Recognized text: $text")
            submitUserMessage(text)
        }
        speechRecognizerManager.onError = { error ->
            Log.e("TADASHI-VOICE", "Speech recognition error: $error")
            _events.value = VoiceEvents.ShowToast("Speech recognition error: $error")
        }
    }

    fun checkPermission() {
        _uiState.value = _uiState.value.copy(
            hasPermission = speechRecognizerManager.hasMicrophonePermission()
        )
    }

    fun requestPermission() {
        _events.value = VoiceEvents.RequestMicrophonePermission
    }

    fun startListening() {
        if (!speechRecognizerManager.hasMicrophonePermission()) {
            requestPermission()
            return
        }
        speechRecognizerManager.startListening()
    }

    fun stopListening() {
        speechRecognizerManager.stopListening()
    }

    fun submitUserMessage(text: String) {
        Log.d("TADASHI-VOICE", "submitUserMessage() called with: $text")
        if (text.isBlank()) return

        val userMessage = ChatMessage(text = text, sender = Sender.USER)
        val updatedChatHistory = _uiState.value.chatHistory + userMessage
        _uiState.value = _uiState.value.copy(chatHistory = updatedChatHistory, userInput = "")

        val assistantResponse = assistantEngine.generateResponse(text)
        Log.d("TADASHI-VOICE", "Assistant response: $assistantResponse")
        val assistantMessage = ChatMessage(text = assistantResponse, sender = Sender.ASSISTANT)
        _uiState.value = _uiState.value.copy(chatHistory = _uiState.value.chatHistory + assistantMessage)

        Log.d("TADASHI-VOICE", "TTS started")
        textToSpeechManager.speak(assistantResponse)
    }

    fun onUserInputChange(text: String) {
        _uiState.value = _uiState.value.copy(userInput = text)
    }

    fun onEventConsumed() {
        _events.value = null
    }

    fun initializeTextToSpeech() {
        textToSpeechManager.initialize()
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizerManager.destroy()
        textToSpeechManager.shutdown()
    }
}
