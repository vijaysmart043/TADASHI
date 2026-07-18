package com.vijay.tadashi.core.voice

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SpeechRecognizerManager(
    private val context: Context
) {

    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    var onListeningStarted: (() -> Unit)? = null
    var onListeningStopped: (() -> Unit)? = null
    var onResult: ((String) -> Unit)? = null
    var onError: ((Int) -> Unit)? = null

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d("SpeechRecognizer", "onReadyForSpeech")
        }

        override fun onBeginningOfSpeech() {
            Log.d("SpeechRecognizer", "onBeginningOfSpeech")
        }

        override fun onRmsChanged(rmsdB: Float) {
        }

        override fun onBufferReceived(buffer: ByteArray?) {
        }

        override fun onEndOfSpeech() {
            Log.d("SpeechRecognizer", "onEndOfSpeech")
            stopListening()
        }

        override fun onError(error: Int) {
            Log.e("SpeechRecognizer", "Error code: $error")
            _isListening.value = false
            onError?.invoke(error)
            onListeningStopped?.invoke()
        }

        override fun onResults(results: Bundle?) {
            Log.d("SpeechRecognizer", "onResults")
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                onResult?.invoke(matches[0])
            }
            _isListening.value = false
            onListeningStopped?.invoke()
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                onResult?.invoke(matches[0])
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
        }
    }

    fun hasMicrophonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun startListening() {
        if (!hasMicrophonePermission()) {
            onError?.invoke(SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS)
            return
        }

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(recognitionListener)
            }
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer?.startListening(intent)
        _isListening.value = true
        onListeningStarted?.invoke()
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
        onListeningStopped?.invoke()
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
