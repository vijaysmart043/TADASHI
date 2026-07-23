package com.vijay.tadashi.core.tools.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidClipboardController @Inject constructor(
    @ApplicationContext private val context: Context
) : ClipboardController {
    private val clipboardManager: ClipboardManager? =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

    override fun copy(text: String): Result<Unit> {
        return runCatching {
            val manager = clipboardManager ?: error("CLIPBOARD_SERVICE unavailable")
            manager.setPrimaryClip(ClipData.newPlainText("TADASHI", text))
        }
    }

    override fun read(): ClipboardReadResult {
        val manager = clipboardManager
            ?: return ClipboardReadResult(
                text = null,
                restricted = false,
                message = "Clipboard unavailable",
                error = "CLIPBOARD_SERVICE unavailable"
            )

        return runCatching {
            val clip = manager.primaryClip
            val item = clip?.getItemAt(0)
            val text = item?.coerceToText(context)?.toString()
            if (text.isNullOrBlank()) {
                ClipboardReadResult(
                    text = null,
                    restricted = false,
                    message = "Clipboard is empty",
                    error = null
                )
            } else {
                ClipboardReadResult(
                    text = text,
                    restricted = false,
                    message = "Clipboard read",
                    error = null
                )
            }
        }.getOrElse { e ->
            ClipboardReadResult(
                text = null,
                restricted = e is SecurityException,
                message = if (e is SecurityException) {
                    "Clipboard access restricted by Android. Keep the app in the foreground and try again."
                } else {
                    "Failed to read clipboard"
                },
                error = e.message ?: e::class.java.simpleName
            )
        }
    }

    override fun clear(): Result<Unit> {
        return runCatching {
            val manager = clipboardManager ?: error("CLIPBOARD_SERVICE unavailable")
            manager.setPrimaryClip(ClipData.newPlainText("TADASHI", ""))
        }
    }

    override fun share(text: String): Result<Unit> {
        return runCatching {
            val sendIntent = Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, text)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            val chooser = Intent.createChooser(sendIntent, "Share clipboard")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    }
}

