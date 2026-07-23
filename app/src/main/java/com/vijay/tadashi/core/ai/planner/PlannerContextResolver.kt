package com.vijay.tadashi.core.ai.planner

import com.vijay.tadashi.core.ai.conversation.ConversationHistory
import com.vijay.tadashi.core.ai.conversation.ConversationRole

object PlannerContextResolver {
    enum class DeviceTarget {
        BRIGHTNESS,
        VOLUME
    }

    fun resolveLastOpenedApp(history: ConversationHistory): String? {
        val messages = history.messages.asReversed()

        for (m in messages) {
            val text = m.text.trim()
            if (m.role == ConversationRole.ASSISTANT) {
                val opened = Regex("^opened\\s+(.+)$", RegexOption.IGNORE_CASE).find(text)
                if (opened != null) {
                    return opened.groupValues[1].trim().lowercase()
                }
            }

            if (m.role == ConversationRole.USER) {
                val open = Regex("^(open|launch|start|run|bring up)\\s+(the\\s+)?(.+)$")
                    .find(text.lowercase())
                if (open != null) {
                    val app = open.groupValues[3].trim()
                    if (app.isNotBlank() && app != "it" && app != "that") {
                        return app.trim('.', '!', '?', ',', ':', ';').lowercase()
                    }
                }
            }
        }

        return null
    }

    fun resolveLastDeviceTarget(history: ConversationHistory): DeviceTarget? {
        val messages = history.messages.asReversed()
        for (m in messages) {
            val text = m.text.lowercase()
            when {
                text.contains("brightness") -> return DeviceTarget.BRIGHTNESS
                text.contains("volume") -> return DeviceTarget.VOLUME
            }
        }
        return null
    }
}

