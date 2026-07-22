package com.vijay.tadashi.core.ai.planner

import com.vijay.tadashi.core.ai.conversation.ConversationHistory

interface PlannerRule {
    val id: String

    fun match(
        history: ConversationHistory,
        normalizedMessage: String
    ): PlannerDecision.Tool?
}
