package com.vijay.tadashi.core.ai.planner

object MultiActionSegmenter {
    enum class Connector {
        START,
        AND,
        THEN
    }

    data class ActionSegment(
        val text: String,
        val connectorBefore: Connector
    )

    fun segment(normalizedMessage: String): List<ActionSegment> {
        val message = normalizedMessage
            .replace(",", " and ")
            .replace(Regex("\\s+"), " ")
            .replace(" and then ", " then ")
            .trim()

        if (message.isBlank()) return emptyList()

        val connectorRegex = Regex("\\s+(and then|then|and)\\s+")
        val segments = mutableListOf<ActionSegment>()
        var cursor = 0
        var connector = Connector.START

        for (m in connectorRegex.findAll(message)) {
            val part = message.substring(cursor, m.range.first).trim()
            if (part.isNotBlank()) {
                segments.add(ActionSegment(text = part, connectorBefore = connector))
            }
            connector = when (m.value.trim()) {
                "then",
                "and then" -> Connector.THEN
                else -> Connector.AND
            }
            cursor = m.range.last + 1
        }

        val tail = message.substring(cursor).trim()
        if (tail.isNotBlank()) {
            segments.add(ActionSegment(text = tail, connectorBefore = connector))
        }

        return segments
    }
}

