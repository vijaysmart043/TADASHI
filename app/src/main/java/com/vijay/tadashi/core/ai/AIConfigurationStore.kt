package com.vijay.tadashi.core.ai

import kotlinx.coroutines.flow.Flow

/**
 * Persistence boundary for [AIConfiguration].
 *
 * Implementations must store sensitive values (e.g., API keys) using encrypted persistence.
 */
interface AIConfigurationStore {
    /**
     * Reads the latest persisted configuration.
     */
    fun getConfiguration(): AIConfiguration

    /**
     * Persists the full configuration atomically.
     */
    fun saveConfiguration(configuration: AIConfiguration)

    /**
     * Emits updates whenever the persisted configuration changes.
     */
    val configurationFlow: Flow<AIConfiguration>
}

