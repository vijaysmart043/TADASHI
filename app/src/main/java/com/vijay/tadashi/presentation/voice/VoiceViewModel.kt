package com.vijay.tadashi.presentation.voice

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vijay.tadashi.core.ai.AssistantEngine
import com.vijay.tadashi.core.ai.AIState
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
/**
 * Coordinates the voice conversation pipeline.
 *
 * The view model depends only on [AssistantEngine] and remains provider-agnostic.
 */
class VoiceViewModel @Inject constructor(
    private val speechRecognizerManager: SpeechRecognizerManager,
    private val textToSpeechManager: TextToSpeechManager,
    private val assistantEngine: AssistantEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceUiState())
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<VoiceEvents?>(null)
    val events: StateFlow<VoiceEvents?> = _events.asStateFlow()

    private val _aiState = MutableStateFlow<AIState>(AIState.Idle)
    val aiState: StateFlow<AIState> = _aiState.asStateFlow()

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

    /**
     * Adds a user message to the conversation and asynchronously generates the assistant response.
     */
    fun submitUserMessage(text: String) {
        Log.d("TADASHI-VOICE", "submitUserMessage() called with: $text")
        if (text.isBlank()) return

        val userMessage = ChatMessage(text = text, sender = Sender.USER)
        val updatedChatHistory = _uiState.value.chatHistory + userMessage
        _uiState.value = _uiState.value.copy(chatHistory = updatedChatHistory, userInput = "")

        viewModelScope.launch {
            _aiState.value = AIState.Loading

            val result = assistantEngine.generateResponse(text)

            if (result.success) {
                Log.d("TADASHI-VOICE", "Assistant response: ${result.text}")
                val assistantMessage = ChatMessage(text = result.text, sender = Sender.ASSISTANT)
                _uiState.value = _uiState.value.copy(chatHistory = _uiState.value.chatHistory + assistantMessage)

                Log.d("TADASHI-VOICE", "TTS started")
                textToSpeechManager.speak(result.text)

                _aiState.value = AIState.Success(result)
            } else {
                val rawMessage = result.error ?: "AI request failed"
                val isGeminiApiKeyMissing =
                    result.provider == com.vijay.tadashi.core.ai.AIProvider.GEMINI &&
                        rawMessage.contains("api key", ignoreCase = true)
                val message = if (isGeminiApiKeyMissing) "API key required" else rawMessage

                Log.e("TADASHI-VOICE", "Assistant error: $message")
                _aiState.value = AIState.Error(message = message, provider = result.provider)

                _events.value = if (isGeminiApiKeyMissing) {
                    VoiceEvents.NavigateToSettings(message)
                } else {
                    VoiceEvents.ShowToast(message)
                }
            }
        }
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
