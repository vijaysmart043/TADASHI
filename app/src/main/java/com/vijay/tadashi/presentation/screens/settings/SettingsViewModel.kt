package com.vijay.tadashi.presentation.screens.settings

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.vijay.tadashi.core.ai.AIConfiguration
import com.vijay.tadashi.core.ai.AIConfigurationStore
import com.vijay.tadashi.core.ai.AIProvider
import com.vijay.tadashi.core.ai.conversation.ConversationHistory
import com.vijay.tadashi.core.ai.repository.AIRepository
import com.vijay.tadashi.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
/**
 * Manages persisted AI provider settings.
 */
class SettingsViewModel @Inject constructor(
    private val configurationStore: AIConfigurationStore,
    private val aiRepository: AIRepository
) : BaseViewModel<SettingsUiState, SettingsEffect, SettingsAction>(SettingsUiState()) {

    init {
        val config = configurationStore.getConfiguration()
        setState(
            state.value.copy(
                provider = config.selectedProvider,
                apiKey = config.apiKey,
                modelName = config.modelName,
                temperature = config.temperature,
                maxTokens = config.maxTokens.toString()
            )
        )
    }

    override fun handleActions() {
        viewModelScope.launch {
            action.collectLatest { action ->
                when (action) {
                    is SettingsAction.ProviderChanged -> {
                        setState(state.value.copy(provider = action.provider))
                    }

                    is SettingsAction.ApiKeyChanged -> {
                        setState(state.value.copy(apiKey = action.value))
                    }

                    is SettingsAction.ModelChanged -> {
                        setState(state.value.copy(modelName = action.value))
                    }

                    is SettingsAction.TemperatureChanged -> {
                        setState(state.value.copy(temperature = action.value))
                    }

                    is SettingsAction.MaxTokensChanged -> {
                        setState(state.value.copy(maxTokens = action.value))
                    }

                    SettingsAction.SaveClicked -> {
                        save()
                    }

                    SettingsAction.TestGeminiConnectionClicked -> {
                        testGeminiConnection()
                    }
                }
            }
        }
    }

    private fun save() {
        val error = validate(state.value)
        if (error != null) {
            sendEffect(SettingsEffect.ShowMessage(error))
            return
        }

        val configuration = AIConfiguration(
            selectedProvider = state.value.provider,
            apiKey = state.value.apiKey.trim(),
            modelName = state.value.modelName.trim(),
            temperature = state.value.temperature,
            maxTokens = state.value.maxTokens.toInt()
        )

        Log.d(TAG, "Selected provider saved: ${configuration.selectedProvider}")
        configurationStore.saveConfiguration(configuration)
        sendEffect(SettingsEffect.ShowMessage("AI settings saved"))
    }

    private fun testGeminiConnection() {
        viewModelScope.launch {
            val configuration = configurationStore.getConfiguration()
            if (configuration.apiKey.isBlank()) {
                sendEffect(SettingsEffect.ShowMessage("API key required"))
                return@launch
            }
            if (configuration.modelName.isBlank()) {
                sendEffect(SettingsEffect.ShowMessage("Model name is required for GEMINI"))
                return@launch
            }

            val result = aiRepository.generateResponse(
                provider = AIProvider.GEMINI,
                history = ConversationHistory(),
                latestUserMessage = "Reply with OK.",
                configuration = configuration
            )

            if (result.success) {
                sendEffect(SettingsEffect.ShowMessage("✓ Gemini Connected"))
            } else {
                sendEffect(SettingsEffect.ShowMessage(result.error ?: "Gemini connection failed"))
            }
        }
    }

    private fun validate(uiState: SettingsUiState): String? {
        val maxTokens = uiState.maxTokens.toIntOrNull()
        if (maxTokens == null || maxTokens <= 0) return "Max tokens must be a positive number"
        if (uiState.temperature < 0f || uiState.temperature > 1f) return "Temperature must be between 0.0 and 1.0"

        return when (uiState.provider) {
            AIProvider.RULE_BASED -> null

            AIProvider.GEMINI,
            AIProvider.OPENAI,
            AIProvider.OLLAMA -> {
                if (uiState.apiKey.isBlank()) return "API key is required for ${uiState.provider.name}"
                if (uiState.modelName.isBlank()) return "Model name is required for ${uiState.provider.name}"
                null
            }
        }
    }

    private companion object {
        private const val TAG = "TADASHI-GEMINI"
    }
}

/**
 * Immutable UI state for the Settings screen.
 */
data class SettingsUiState(
    val provider: AIProvider = AIProvider.RULE_BASED,
    val apiKey: String = "",
    val modelName: String = "",
    val temperature: Float = 0.7f,
    val maxTokens: String = "512"
)

sealed interface SettingsEffect {
    data class ShowMessage(
        val message: String
    ) : SettingsEffect
}

sealed interface SettingsAction {
    data class ProviderChanged(
        val provider: AIProvider
    ) : SettingsAction

    data class ApiKeyChanged(
        val value: String
    ) : SettingsAction

    data class ModelChanged(
        val value: String
    ) : SettingsAction

    data class TemperatureChanged(
        val value: Float
    ) : SettingsAction

    data class MaxTokensChanged(
        val value: String
    ) : SettingsAction

    data object SaveClicked : SettingsAction

    data object TestGeminiConnectionClicked : SettingsAction
}
