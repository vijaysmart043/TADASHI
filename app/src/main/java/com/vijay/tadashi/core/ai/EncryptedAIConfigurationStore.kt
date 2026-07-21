package com.vijay.tadashi.core.ai

import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

/**
 * Stores [AIConfiguration] using encrypted [SharedPreferences].
 */
class EncryptedAIConfigurationStore @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : AIConfigurationStore {

    override fun getConfiguration(): AIConfiguration {
        val providerName = sharedPreferences.getString(KEY_PROVIDER, AIProvider.RULE_BASED.name)
            ?: AIProvider.RULE_BASED.name

        val provider = runCatching { AIProvider.valueOf(providerName) }
            .getOrDefault(AIProvider.RULE_BASED)

        return AIConfiguration(
            selectedProvider = provider,
            apiKey = sharedPreferences.getString(KEY_API_KEY, "") ?: "",
            modelName = sharedPreferences.getString(KEY_MODEL, "") ?: "",
            temperature = sharedPreferences.getFloat(KEY_TEMPERATURE, DEFAULT_TEMPERATURE),
            maxTokens = sharedPreferences.getInt(KEY_MAX_TOKENS, DEFAULT_MAX_TOKENS)
        )
    }

    override fun saveConfiguration(configuration: AIConfiguration) {
        sharedPreferences.edit()
            .putString(KEY_PROVIDER, configuration.selectedProvider.name)
            .putString(KEY_API_KEY, configuration.apiKey)
            .putString(KEY_MODEL, configuration.modelName)
            .putFloat(KEY_TEMPERATURE, configuration.temperature)
            .putInt(KEY_MAX_TOKENS, configuration.maxTokens)
            .apply()
    }

    override val configurationFlow: Flow<AIConfiguration> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(getConfiguration())
        }

        trySend(getConfiguration())
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    private companion object {
        private const val KEY_PROVIDER = "ai_provider"
        private const val KEY_API_KEY = "ai_api_key"
        private const val KEY_MODEL = "ai_model_name"
        private const val KEY_TEMPERATURE = "ai_temperature"
        private const val KEY_MAX_TOKENS = "ai_max_tokens"

        private const val DEFAULT_TEMPERATURE = 0.7f
        private const val DEFAULT_MAX_TOKENS = 512
    }
}

