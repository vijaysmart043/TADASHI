package com.vijay.tadashi.core.ai.planner

import com.vijay.tadashi.core.ai.conversation.ConversationHistory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntentClassifier @Inject constructor() {
    fun classify(
        history: ConversationHistory,
        normalizedMessage: String
    ): IntentResult {
        val candidates = buildList {
            add(classifyOpenApp(normalizedMessage))
            add(classifyCloseApp(normalizedMessage))
            add(classifyFlashlight(normalizedMessage))
            add(classifyGeneralChat(normalizedMessage))
            add(classifyQuestion(normalizedMessage))
            add(classifySearch(normalizedMessage))
            add(classifyDeviceControl(normalizedMessage))
        }

        return candidates
            .maxByOrNull { it.confidence }
            ?.takeIf { it.confidence > 0.0 }
            ?: IntentResult(
                intent = IntentCategory.UNKNOWN,
                confidence = 0.2,
                reason = "No matching intent"
            )
    }

    private fun classifyOpenApp(message: String): IntentResult {
        if (looksLikeGreetingWithAppName(message)) {
            return IntentResult(
                intent = IntentCategory.UNKNOWN,
                confidence = 0.30,
                reason = "Greeting + app name without launcher verb"
            )
        }

        val strong = Regex("^(open|launch|start|run|bring up)\\s+(the\\s+)?(.+)$")
        val polite = Regex("^(can you|could you|please|would you)\\s+(open|launch|start|run|bring up)\\s+(the\\s+)?(.+)$")
        val hedged = Regex("^(maybe|perhaps)\\s+(open|launch|start|run|bring up)\\s+(the\\s+)?(.+)$")

        fun build(appRaw: String, confidence: Double, reason: String): IntentResult {
            val app = appRaw.trim().trim('.', '!', '?', ',', ':', ';')
            if (app.isBlank()) {
                return IntentResult(IntentCategory.UNKNOWN, 0.0, "Empty app name")
            }
            return IntentResult(
                intent = IntentCategory.OPEN_APP,
                confidence = confidence,
                reason = reason,
                slots = mapOf("app" to app)
            )
        }

        hedged.find(message)?.let { m ->
            return build(
                appRaw = m.groupValues.last(),
                confidence = 0.72,
                reason = "Matched hedged launcher intent"
            )
        }

        polite.find(message)?.let { m ->
            return build(
                appRaw = m.groupValues.last(),
                confidence = 0.90,
                reason = "Matched polite launcher intent"
            )
        }

        strong.find(message)?.let { m ->
            return build(
                appRaw = m.groupValues.last(),
                confidence = 0.99,
                reason = "Matched launcher intent"
            )
        }

        if (message == "open browser" || message == "launch browser" || message == "start browser") {
            return IntentResult(
                intent = IntentCategory.OPEN_APP,
                confidence = 0.95,
                reason = "Matched browser synonym",
                slots = mapOf("app" to "browser")
            )
        }

        return IntentResult(
            intent = IntentCategory.UNKNOWN,
            confidence = 0.0,
            reason = "No launcher match"
        )
    }

    private fun classifyFlashlight(message: String): IntentResult {
        val strongOn = listOf(
            Regex("^turn on (the )?(flashlight|torch|light)$"),
            Regex("^switch on (the )?(flashlight|torch|light)$"),
            Regex("^enable (the )?(flashlight|torch|light)$"),
            Regex("^(flashlight|torch|light) on$")
        )
        val strongOff = listOf(
            Regex("^turn off (the )?(flashlight|torch|light)$"),
            Regex("^switch off (the )?(flashlight|torch|light)$"),
            Regex("^disable (the )?(flashlight|torch|light)$"),
            Regex("^(flashlight|torch|light) off$")
        )
        val strongToggle = listOf(
            Regex("^toggle (the )?(flashlight|torch|light)$")
        )

        val politePrefix = Regex("^(can you|could you|please|would you)\\s+")
        val hedgedPrefix = Regex("^(maybe|perhaps)\\s+")

        fun confidenceFor(message: String, base: Double): Pair<Double, String> {
            return when {
                hedgedPrefix.containsMatchIn(message) -> 0.72 to "Matched hedged flashlight intent"
                politePrefix.containsMatchIn(message) -> 0.90 to "Matched polite flashlight intent"
                else -> base to "Matched flashlight intent"
            }
        }

        val stripped = message
            .replace(politePrefix, "")
            .replace(hedgedPrefix, "")
            .trim()

        val action = when {
            strongToggle.any { it.matches(stripped) } -> "TOGGLE"
            strongOn.any { it.matches(stripped) } -> "ON"
            strongOff.any { it.matches(stripped) } -> "OFF"
            stripped.contains("toggle") && mentionsFlashTerms(stripped) -> "TOGGLE"
            stripped.contains("on") && mentionsFlashTerms(stripped) -> "ON"
            stripped.contains("off") && mentionsFlashTerms(stripped) -> "OFF"
            else -> null
        } ?: return IntentResult(
            intent = IntentCategory.UNKNOWN,
            confidence = 0.0,
            reason = "No flashlight match"
        )

        val (confidence, reason) = confidenceFor(message, 0.98)
        return IntentResult(
            intent = IntentCategory.FLASHLIGHT,
            confidence = confidence,
            reason = reason,
            slots = mapOf("action" to action)
        )
    }

    private fun classifyCloseApp(message: String): IntentResult {
        val strong = Regex("^(close|exit|quit)\\s+(the\\s+)?(.+)$")
        val polite = Regex("^(can you|could you|please|would you)\\s+(close|exit|quit)\\s+(the\\s+)?(.+)$")
        val hedged = Regex("^(maybe|perhaps)\\s+(close|exit|quit)\\s+(the\\s+)?(.+)$")

        fun build(appRaw: String, confidence: Double, reason: String): IntentResult {
            val app = appRaw.trim().trim('.', '!', '?', ',', ':', ';')
            if (app.isBlank() || app == "it" || app == "that") {
                return IntentResult(IntentCategory.UNKNOWN, 0.0, "Missing app name")
            }
            return IntentResult(
                intent = IntentCategory.CLOSE_APP,
                confidence = confidence,
                reason = reason,
                slots = mapOf("app" to app)
            )
        }

        hedged.find(message)?.let { m ->
            return build(
                appRaw = m.groupValues.last(),
                confidence = 0.72,
                reason = "Matched hedged close app intent"
            )
        }

        polite.find(message)?.let { m ->
            return build(
                appRaw = m.groupValues.last(),
                confidence = 0.90,
                reason = "Matched polite close app intent"
            )
        }

        strong.find(message)?.let { m ->
            return build(
                appRaw = m.groupValues.last(),
                confidence = 0.95,
                reason = "Matched close app intent"
            )
        }

        return IntentResult(
            intent = IntentCategory.UNKNOWN,
            confidence = 0.0,
            reason = "No close app match"
        )
    }

    private fun classifyGeneralChat(message: String): IntentResult {
        val greetings = setOf("hi", "hello", "hey")
        if (message in greetings) {
            return IntentResult(
                intent = IntentCategory.GENERAL_CHAT,
                confidence = 0.95,
                reason = "Matched greeting"
            )
        }

        if (message == "who are you" || message == "what are you" || message == "how are you") {
            return IntentResult(
                intent = IntentCategory.GENERAL_CHAT,
                confidence = 0.95,
                reason = "Matched assistant small-talk"
            )
        }

        return IntentResult(
            intent = IntentCategory.UNKNOWN,
            confidence = 0.0,
            reason = "No general chat match"
        )
    }

    private fun classifyQuestion(message: String): IntentResult {
        val questionPrefixes = listOf(
            "what is ",
            "who is ",
            "who invented ",
            "explain ",
            "tell me about ",
            "how does ",
            "why "
        )
        val looksLikeQuestion = questionPrefixes.any { message.startsWith(it) } || message.endsWith("?")
        if (!looksLikeQuestion) {
            return IntentResult(IntentCategory.UNKNOWN, 0.0, "No question match")
        }

        return IntentResult(
            intent = IntentCategory.QUESTION,
            confidence = 0.90,
            reason = "Matched question pattern"
        )
    }

    private fun classifySearch(message: String): IntentResult {
        val searchPrefixes = listOf(
            "search for ",
            "google ",
            "look up ",
            "find "
        )
        if (searchPrefixes.any { message.startsWith(it) }) {
            return IntentResult(
                intent = IntentCategory.SEARCH,
                confidence = 0.85,
                reason = "Matched search intent"
            )
        }

        return IntentResult(IntentCategory.UNKNOWN, 0.0, "No search match")
    }

    private fun classifyDeviceControl(message: String): IntentResult {
        val hasBrightness = message.contains("brightness")
        val hasVolume = message.contains("volume")
        val hasConnectivity = message.contains("wifi") || message.contains("wi-fi") || message.contains("bluetooth")

        if (!hasBrightness && !hasVolume && !hasConnectivity) {
            return IntentResult(IntentCategory.UNKNOWN, 0.0, "No device control match")
        }

        val strongVerb = listOf(
            "set ",
            "increase ",
            "decrease ",
            "mute ",
            "turn ",
            "enable ",
            "disable "
        ).any { message.startsWith(it) }

        val confidence = when {
            (hasBrightness || hasVolume) && strongVerb -> 0.92
            (hasBrightness || hasVolume) -> 0.80
            hasConnectivity && strongVerb -> 0.75
            else -> 0.70
        }

        val reason = when {
            hasBrightness -> "Matched brightness device control"
            hasVolume -> "Matched volume device control"
            else -> "Matched connectivity device control"
        }

        return IntentResult(
            intent = IntentCategory.DEVICE_CONTROL,
            confidence = confidence,
            reason = reason
        )
    }

    private fun mentionsFlashTerms(message: String): Boolean {
        return message.contains("flashlight") || message.contains("torch") || message.contains("light")
    }

    private fun looksLikeGreetingWithAppName(message: String): Boolean {
        val greetings = listOf("hello ", "hi ", "hey ")
        if (greetings.none { message.startsWith(it) }) return false
        val remainder = greetings.firstNotNullOfOrNull { prefix ->
            if (message.startsWith(prefix)) message.removePrefix(prefix) else null
        } ?: return false
        return remainder.isNotBlank()
    }
}
