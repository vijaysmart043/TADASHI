package com.vijay.tadashi.core.ai

import android.util.Log
import com.vijay.tadashi.core.ai.planner.AIToolPlanner
import com.vijay.tadashi.core.ai.planner.PlannerDecision
import com.vijay.tadashi.core.ai.planner.PlannerLogger
import com.vijay.tadashi.core.ai.conversation.ConversationHistory
import com.vijay.tadashi.core.ai.streaming.StreamingResponse
import com.vijay.tadashi.core.tools.ToolExecutor
import com.vijay.tadashi.core.tools.ToolResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * Single entry-point [AssistantEngine] that routes requests to the configured provider engine.
 *
 * This keeps the presentation layer stable while new providers are introduced.
 */
class ProviderAssistantEngine @Inject constructor(
    private val configurationStore: AIConfigurationStore,
    private val ruleBasedAssistantEngine: RuleBasedAssistantEngine,
    private val geminiAssistantEngine: GeminiAssistantEngine,
    private val planner: AIToolPlanner,
    private val toolExecutor: ToolExecutor
) : AssistantEngine {

    override suspend fun generateResponse(
        history: ConversationHistory,
        latestUserMessage: String
    ): AIResult {
        val provider = configurationStore.getConfiguration().selectedProvider
        Log.d(TAG, "Selected provider: $provider")

        return when (provider) {
            AIProvider.RULE_BASED -> {
                Log.d(TAG, "Engine selected: RuleBasedAssistantEngine")
                ruleBasedAssistantEngine.generateResponse(
                    history = history,
                    latestUserMessage = latestUserMessage
                )
            }

            AIProvider.GEMINI -> {
                Log.d(TAG, "Engine selected: GeminiAssistantEngine")
                val plan = planner.plan(
                    history = history,
                    latestUserMessage = latestUserMessage
                )

                when (val decision = plan.decision) {
                    is PlannerDecision.Tool -> {
                        PlannerLogger.d("Routing to ToolExecutor")
                        val toolResult = toolExecutor.execute(decision.request)
                        toolResult.toAIResult(provider = provider)
                    }

                    PlannerDecision.ContinueToGemini -> {
                        PlannerLogger.d("Routing to Gemini")
                        geminiAssistantEngine.generateResponse(
                            history = history,
                            latestUserMessage = latestUserMessage
                        )
                    }
                }
            }

            AIProvider.OPENAI,
            AIProvider.OLLAMA -> AIResult(
                text = "",
                success = false,
                error = "Provider not implemented: $provider",
                provider = provider
            )
        }
    }

    override fun streamResponse(
        history: ConversationHistory,
        latestUserMessage: String
    ): Flow<StreamingResponse> {
        val provider = configurationStore.getConfiguration().selectedProvider
        Log.d(TAG, "Selected provider: $provider")

        return when (provider) {
            AIProvider.RULE_BASED -> {
                Log.d(TAG, "Engine selected: RuleBasedAssistantEngine")
                ruleBasedAssistantEngine.streamResponse(
                    history = history,
                    latestUserMessage = latestUserMessage
                )
            }

            AIProvider.GEMINI -> {
                Log.d(TAG, "Engine selected: GeminiAssistantEngine")
                val plan = planner.plan(
                    history = history,
                    latestUserMessage = latestUserMessage
                )

                when (val decision = plan.decision) {
                    is PlannerDecision.Tool -> {
                        flow {
                            PlannerLogger.d("Routing to ToolExecutor")
                            val toolResult = toolExecutor.execute(decision.request)
                            val message = toolResult.toDisplayMessage()
                            emit(StreamingResponse.Completed(message))
                        }
                    }

                    PlannerDecision.ContinueToGemini -> {
                        PlannerLogger.d("Routing to Gemini")
                        geminiAssistantEngine.streamResponse(
                            history = history,
                            latestUserMessage = latestUserMessage
                        )
                    }
                }
            }

            AIProvider.OPENAI,
            AIProvider.OLLAMA -> flowOf(
                StreamingResponse.Error("Provider not implemented: $provider")
            )
        }
    }

    private fun ToolResult.toAIResult(provider: AIProvider): AIResult {
        return when (this) {
            is ToolResult.Success -> AIResult(
                text = message,
                success = true,
                provider = provider
            )

            is ToolResult.Failure -> AIResult(
                text = message,
                success = false,
                error = message,
                provider = provider
            )

            is ToolResult.PermissionDenied -> AIResult(
                text = "Permission denied",
                success = false,
                error = "Permission denied",
                provider = provider
            )

            is ToolResult.Unsupported -> AIResult(
                text = message,
                success = false,
                error = message,
                provider = provider
            )

            is ToolResult.Cancelled -> AIResult(
                text = message,
                success = false,
                error = message,
                provider = provider
            )
        }
    }

    private fun ToolResult.toDisplayMessage(): String {
        return when (this) {
            is ToolResult.Success -> message
            is ToolResult.Failure -> message
            is ToolResult.Unsupported -> message
            is ToolResult.Cancelled -> message
            is ToolResult.PermissionDenied -> "Permission denied"
        }
    }

    private companion object {
        private const val TAG = "TADASHI-GEMINI"
    }
}
