package com.vijay.tadashi.core.ai.planner

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParameterExtractor @Inject constructor() {
    fun extract(
        normalizedMessage: String,
        intentResult: IntentResult
    ): ParameterExtraction? {
        return when (intentResult.intent) {
            IntentCategory.OPEN_APP -> extractOpenApp(normalizedMessage, intentResult)
            IntentCategory.CLOSE_APP -> null
            IntentCategory.FLASHLIGHT -> extractFlashlight(normalizedMessage, intentResult)
            IntentCategory.DEVICE_CONTROL -> extractDeviceControl(normalizedMessage)
            IntentCategory.SEARCH,
            IntentCategory.GENERAL_CHAT,
            IntentCategory.QUESTION,
            IntentCategory.UNKNOWN -> null
        }
    }

    private fun extractOpenApp(
        normalizedMessage: String,
        intentResult: IntentResult
    ): ParameterExtraction? {
        val app = intentResult.slots["app"]
            ?: return null

        return ParameterExtraction(
            toolId = "OPEN_APP",
            arguments = mapOf(
                "app" to app
            ),
            reason = "Extracted app=$app"
        )
    }

    private fun extractFlashlight(
        normalizedMessage: String,
        intentResult: IntentResult
    ): ParameterExtraction? {
        val action = intentResult.slots["action"]
            ?: return null

        return ParameterExtraction(
            toolId = "FLASHLIGHT",
            arguments = mapOf(
                "action" to action
            ),
            reason = "Extracted action=$action"
        )
    }

    private fun extractDeviceControl(normalizedMessage: String): ParameterExtraction? {
        if (normalizedMessage.contains("brightness")) return extractBrightness(normalizedMessage)
        if (normalizedMessage.contains("volume")) return extractVolume(normalizedMessage)
        return null
    }

    private fun extractBrightness(message: String): ParameterExtraction? {
        val mute = Regex("^mute\\s+brightness$").matches(message)
        if (mute) return null

        fun clampPercent(raw: String): Pair<String, String?> {
            val intValue = raw.toIntOrNull() ?: return raw to "Invalid percent"
            val clamped = intValue.coerceIn(0, 100)
            return clamped.toString() to if (clamped != intValue) "Clamped $intValue to $clamped" else null
        }

        val set = Regex("^(set\\s+)?brightness\\s*(to\\s+)?(\\d{1,3})%?$").find(message)
            ?: Regex("^(increase|raise)\\s+brightness\\s+to\\s+(\\d{1,3})%?$").find(message)
            ?: Regex("^(decrease|reduce|lower)\\s+brightness\\s+to\\s+(\\d{1,3})%?$").find(message)
            ?: Regex("^brightness\\s+to\\s+(\\d{1,3})%?$").find(message)

        if (set != null) {
            val raw = set.groupValues.last()
            val (value, note) = clampPercent(raw)
            val reason = listOfNotNull("Matched brightness set", note).joinToString(separator = " | ")
            return ParameterExtraction(
                toolId = "BRIGHTNESS",
                arguments = mapOf(
                    "action" to "SET",
                    "value" to value,
                    "unit" to "PERCENT"
                ),
                reason = reason
            )
        }

        val incBy = Regex("^(increase|raise)\\s+brightness\\s+by\\s+(\\d{1,3})%?$").find(message)
        if (incBy != null) {
            val raw = incBy.groupValues.last()
            val (value, note) = clampPercent(raw)
            val reason = listOfNotNull("Matched brightness increase by", note).joinToString(separator = " | ")
            return ParameterExtraction(
                toolId = "BRIGHTNESS",
                arguments = mapOf(
                    "action" to "INCREASE",
                    "value" to value,
                    "unit" to "PERCENT"
                ),
                reason = reason
            )
        }

        val decBy = Regex("^(decrease|reduce|lower)\\s+brightness\\s+by\\s+(\\d{1,3})%?$").find(message)
        if (decBy != null) {
            val raw = decBy.groupValues.last()
            val (value, note) = clampPercent(raw)
            val reason = listOfNotNull("Matched brightness decrease by", note).joinToString(separator = " | ")
            return ParameterExtraction(
                toolId = "BRIGHTNESS",
                arguments = mapOf(
                    "action" to "DECREASE",
                    "value" to value,
                    "unit" to "PERCENT"
                ),
                reason = reason
            )
        }

        val inc = Regex("^increase\\s+brightness$").matches(message)
        if (inc) {
            return ParameterExtraction(
                toolId = "BRIGHTNESS",
                arguments = mapOf(
                    "action" to "INCREASE"
                ),
                reason = "Matched brightness increase"
            )
        }

        val dec = Regex("^decrease\\s+brightness$").matches(message)
        if (dec) {
            return ParameterExtraction(
                toolId = "BRIGHTNESS",
                arguments = mapOf(
                    "action" to "DECREASE"
                ),
                reason = "Matched brightness decrease"
            )
        }

        return null
    }

    private fun extractVolume(message: String): ParameterExtraction? {
        val mute = Regex("^mute\\s+volume$").matches(message)
        if (mute) {
            return ParameterExtraction(
                toolId = "VOLUME",
                arguments = mapOf(
                    "action" to "MUTE"
                ),
                reason = "Matched volume mute"
            )
        }

        fun clampPercent(raw: String): Pair<String, String?> {
            val intValue = raw.toIntOrNull() ?: return raw to "Invalid percent"
            val clamped = intValue.coerceIn(0, 100)
            return clamped.toString() to if (clamped != intValue) "Clamped $intValue to $clamped" else null
        }

        val set = Regex("^(set\\s+)?volume\\s*(to\\s+)?(\\d{1,3})%?$").find(message)
            ?: Regex("^(increase|raise)\\s+volume\\s+to\\s+(\\d{1,3})%?$").find(message)
            ?: Regex("^(decrease|reduce|lower)\\s+volume\\s+to\\s+(\\d{1,3})%?$").find(message)
            ?: Regex("^volume\\s+to\\s+(\\d{1,3})%?$").find(message)
            ?: Regex("^volume\\s+(\\d{1,3})%?$").find(message)

        if (set != null) {
            val raw = set.groupValues.last()
            val (value, note) = clampPercent(raw)
            val reason = listOfNotNull("Matched volume set", note).joinToString(separator = " | ")
            return ParameterExtraction(
                toolId = "VOLUME",
                arguments = mapOf(
                    "action" to "SET",
                    "value" to value,
                    "unit" to "PERCENT"
                ),
                reason = reason
            )
        }

        val incBy = Regex("^(increase|raise)\\s+volume\\s+by\\s+(\\d{1,3})%?$").find(message)
        if (incBy != null) {
            val raw = incBy.groupValues.last()
            val (value, note) = clampPercent(raw)
            val reason = listOfNotNull("Matched volume increase by", note).joinToString(separator = " | ")
            return ParameterExtraction(
                toolId = "VOLUME",
                arguments = mapOf(
                    "action" to "INCREASE",
                    "value" to value,
                    "unit" to "PERCENT"
                ),
                reason = reason
            )
        }

        val decBy = Regex("^(decrease|reduce|lower)\\s+volume\\s+by\\s+(\\d{1,3})%?$").find(message)
        if (decBy != null) {
            val raw = decBy.groupValues.last()
            val (value, note) = clampPercent(raw)
            val reason = listOfNotNull("Matched volume decrease by", note).joinToString(separator = " | ")
            return ParameterExtraction(
                toolId = "VOLUME",
                arguments = mapOf(
                    "action" to "DECREASE",
                    "value" to value,
                    "unit" to "PERCENT"
                ),
                reason = reason
            )
        }

        val inc = Regex("^increase\\s+volume$").matches(message)
        if (inc) {
            return ParameterExtraction(
                toolId = "VOLUME",
                arguments = mapOf(
                    "action" to "INCREASE"
                ),
                reason = "Matched volume increase"
            )
        }

        val dec = Regex("^decrease\\s+volume$").matches(message)
        if (dec) {
            return ParameterExtraction(
                toolId = "VOLUME",
                arguments = mapOf(
                    "action" to "DECREASE"
                ),
                reason = "Matched volume decrease"
            )
        }

        return null
    }
}
