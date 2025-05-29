package vc.prog3c.poe.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageUtils {
    private const val FILE_PROVIDER_AUTHORITY = "vc.prog3c.poe.fileprovider"

    fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
    }

    fun getImagePickerIntent(): Intent {
        return Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    }

    fun getCameraIntent(context: Context, photoFile: File): Intent {
        val photoURI = FileProvider.getUriForFile(
            context,
            FILE_PROVIDER_AUTHORITY,
            photoFile
        )
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        }
    }

    fun getUriFromFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            FILE_PROVIDER_AUTHORITY,
            file
        )
    }
} 