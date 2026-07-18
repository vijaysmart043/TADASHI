package com.vijay.tadashi.presentation.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vijay.tadashi.core.voice.SpeechRecognizerManager
import com.vijay.tadashi.core.voice.TextToSpeechManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoiceViewModel @Inject constructor(
    private val speechRecognizerManager: SpeechRecognizerManager,
    private val textToSpeechManager: TextToSpeechManager
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
            _uiState.value = _uiState.value.copy(isListening = true)
        }
        speechRecognizerManager.onListeningStopped = {
            _uiState.value = _uiState.value.copy(isListening = false)
        }
        speechRecognizerManager.onResult = { text ->
            _uiState.value = _uiState.value.copy(recognizedText = text)
        }
        speechRecognizerManager.onError = { error ->
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

    fun speak(text: String) {
        if (text.isNotEmpty()) {
            textToSpeechManager.speak(text)
        }
    }

    fun clearText() {
        _uiState.value = _uiState.value.copy(recognizedText = "")
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
