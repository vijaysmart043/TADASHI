package com.vijay.tadashi.core.tools.camera

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.vijay.tadashi.core.tools.common.ControllerResult
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidCameraController @Inject constructor(
    @ApplicationContext private val context: Context
) : CameraController {
    override fun openCamera(lensFacing: String?): ControllerResult<Unit> {
        val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            applyLensExtras(lensFacing)
        }
        return start(intent, "OPEN_CAMERA_FAILED")
    }

    override fun openVideoMode(lensFacing: String?): ControllerResult<Unit> {
        val intent = Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            applyLensExtras(lensFacing)
        }
        return start(intent, "OPEN_VIDEO_FAILED")
    }

    override fun takePicture(lensFacing: String?): ControllerResult<Unit> {
        val outputUri = createOutputUri()
            ?: return ControllerResult.Failure(
                message = "Unable to create output file for picture",
                error = "OUTPUT_URI_FAILED",
                permissionStatus = "N/A"
            )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
            applyLensExtras(lensFacing)
        }

        return start(intent, "TAKE_PICTURE_FAILED")
    }

    override fun switchCamera(lensFacing: String?): ControllerResult<Unit> {
        val target = lensFacing?.trim()?.uppercase()
        val newLens = when (target) {
            "FRONT" -> "BACK"
            "BACK" -> "FRONT"
            else -> "FRONT"
        }
        return openCamera(newLens)
    }

    private fun start(intent: Intent, errorCode: String): ControllerResult<Unit> {
        val resolved = intent.resolveActivity(context.packageManager) != null
        if (!resolved) {
            return ControllerResult.Failure(
                message = "No application can handle this request",
                error = "NO_HANDLER",
                permissionStatus = "N/A"
            )
        }
        return runCatching {
            context.startActivity(intent)
            ControllerResult.Success(Unit, permissionStatus = "N/A")
        }.getOrElse { e ->
            ControllerResult.Failure(
                message = e.message ?: "Failed to start camera",
                error = errorCode,
                permissionStatus = "N/A"
            )
        }
    }

    private fun Intent.applyLensExtras(lensFacing: String?) {
        val lens = lensFacing?.trim()?.uppercase() ?: return
        when (lens) {
            "FRONT" -> {
                putExtra("android.intent.extras.CAMERA_FACING", 1)
                putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
                putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
            }
            "BACK" -> {
                putExtra("android.intent.extras.CAMERA_FACING", 0)
                putExtra("android.intent.extra.USE_FRONT_CAMERA", false)
                putExtra("android.intent.extras.LENS_FACING_BACK", 1)
            }
        }
    }

    private fun createOutputUri(): Uri? {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: return null
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "tadashi_${System.currentTimeMillis()}.jpg")
        return runCatching {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        }.getOrNull()
    }
}
