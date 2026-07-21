package com.vijay.tadashi.core.ai

/**
 * Supported assistant providers.
 *
 * Only [RULE_BASED] is fully operational in Phase 3.1. The other providers exist to keep the
 * architecture stable while real networking is added incrementally.
 */
enum class AIProvider {
    RULE_BASED,
    GEMINI,
    OPENAI,
    OLLAMA
}

