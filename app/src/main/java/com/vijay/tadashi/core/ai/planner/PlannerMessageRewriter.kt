package com.vijay.tadashi.core.ai.planner

import com.vijay.tadashi.core.ai.conversation.ConversationHistory

object PlannerMessageRewriter {
    fun rewriteSegment(
        history: ConversationHistory,
        normalizedSegment: String
    ): String {
        var segment = normalizedSegment.trim()
        if (segment.isBlank()) return segment

        segment = rewriteClosePronoun(history, segment)
        segment = rewriteDeviceControlPronoun(history, segment)
        return segment
    }

    private fun rewriteClosePronoun(history: ConversationHistory, segment: String): String {
        val closeIt = Regex("^(close|exit|quit)\\s+(it|that)$").find(segment) ?: return segment
        val app = PlannerContextResolver.resolveLastOpenedApp(history) ?: return segment
        return "${closeIt.groupValues[1]} $app"
    }

    private fun rewriteDeviceControlPronoun(history: ConversationHistory, segment: String): String {
        if (segment.contains("brightness") || segment.contains("volume")) return segment

        val hasControlVerb = listOf(
            "set ",
            "increase ",
            "decrease ",
            "reduce ",
            "lower ",
            "raise ",
            "mute "
        ).any { segment.startsWith(it) }
        if (!hasControlVerb) return segment

        val target = PlannerContextResolver.resolveLastDeviceTarget(history) ?: return segment
        val replacement = when (target) {
            PlannerContextResolver.DeviceTarget.BRIGHTNESS -> "brightness"
            PlannerContextResolver.DeviceTarget.VOLUME -> "volume"
        }

        return segment.replaceFirst(Regex("\\b(it|that)\\b"), replacement)
    }
}

