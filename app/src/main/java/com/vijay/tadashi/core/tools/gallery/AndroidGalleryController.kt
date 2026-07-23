package com.vijay.tadashi.core.tools.gallery

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import com.vijay.tadashi.core.tools.common.ControllerResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidGalleryController @Inject constructor(
    @ApplicationContext private val context: Context
) : GalleryController {
    override fun openGallery(): ControllerResult<Unit> {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            setType("image/*")
        }
        return start(intent, "OPEN_GALLERY_FAILED")
    }

    override fun openLatestImage(): ControllerResult<Unit> {
        if (!hasReadPermission()) {
            return ControllerResult.PermissionDenied(
                permissionStatus = permissionStatus(),
                message = "Missing permission to read images"
            )
        }
        val latest = queryLatestImageUri()
            ?: return ControllerResult.Failure(
                message = "No images found",
                error = "NO_IMAGES",
                permissionStatus = permissionStatus()
            )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(latest, "image/*")
        }
        return start(intent, "OPEN_LATEST_FAILED")
    }

    override fun shareLatestImage(): ControllerResult<Unit> {
        if (!hasReadPermission()) {
            return ControllerResult.PermissionDenied(
                permissionStatus = permissionStatus(),
                message = "Missing permission to read images"
            )
        }
        val latest = queryLatestImageUri()
            ?: return ControllerResult.Failure(
                message = "No images found",
                error = "NO_IMAGES",
                permissionStatus = permissionStatus()
            )
        val intent = Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, latest)
        }
        val chooser = Intent.createChooser(intent, "Share image").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return start(chooser, "SHARE_FAILED")
    }

    private fun start(intent: Intent, errorCode: String): ControllerResult<Unit> {
        val resolved = intent.resolveActivity(context.packageManager) != null
        if (!resolved) {
            return ControllerResult.Failure(
                message = "No application can handle this request",
                error = "NO_HANDLER",
                permissionStatus = permissionStatus()
            )
        }
        return runCatching {
            context.startActivity(intent)
            ControllerResult.Success(Unit, permissionStatus = permissionStatus())
        }.getOrElse { e ->
            ControllerResult.Failure(
                message = e.message ?: "Failed to start gallery",
                error = errorCode,
                permissionStatus = permissionStatus()
            )
        }
    }

    private fun queryLatestImageUri(): android.net.Uri? {
        val projection = arrayOf(
            MediaStore.Images.Media._ID
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        return context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            if (!cursor.moveToFirst()) return@use null
            val id = cursor.getLong(idIndex)
            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
        }
    }

    private fun hasReadPermission(): Boolean {
        val readMediaGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED

        val readExternalGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        return readMediaGranted || readExternalGranted
    }

    private fun permissionStatus(): String {
        return if (hasReadPermission()) "READ_MEDIA_IMAGES_GRANTED" else "READ_MEDIA_IMAGES_DENIED"
    }
}
