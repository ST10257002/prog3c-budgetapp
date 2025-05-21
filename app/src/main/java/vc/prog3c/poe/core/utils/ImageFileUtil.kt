package vc.prog3c.poe.core.utils

import android.net.Uri
import android.os.Environment
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utilities for managing images directories for the application.
 *
 * @author ST10257002
 */
object ImageFileUtil {
// <editor-fold desc="Fields">

    /**
     * A simple date format to generate uniquely timestamped filenames.
     *
     * @see SimpleDateFormat
     * @author ST10257002
     */
    private val timestampFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

// </editor-fold>
// <editor-fold desc="Utilities">

    /**
     * Creates and locates the private storage directory of the application.
     *
     * @param caller This is necessary for functions using Android File APIs
     *        that require a valid context to be called from.
     *
     * @return The private storage directory of the application.
     * @author ST10257002
     */
    fun getStorageDirectory(caller: FragmentActivity): File {
        return File(
            caller.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "BoostaCam"
        ).apply {
            if (exists() == false) {
                mkdirs()
            }
        }
    }


    /**
     * Creates a unique filename for an image and then constructs the private
     * directory of the application with a path to this image file.
     *
     * @param caller This is necessary for functions using Android File APIs
     *        that require a valid context to be called from.
     *
     * @return Directory with a uniquely named image file.
     * @author ST10257002
     */
    fun nameUniqueImageFile(caller: FragmentActivity): File {
        val filename = "IMG_${timestampFormat.format(Date())}.jpg"
        return File(
            getStorageDirectory(caller), filename
        )
    }


    /**
     * Copies an image file from one directory to another.
     *
     * @param caller This is necessary for functions using Android File APIs
     *        that require a valid context to be called from.
     *
     * @throws IOException
     * @return Whether the operation was successful.
     * @author ST10257002
     */
    suspend fun copyUriToDirectory(
        caller: FragmentActivity, sourceImage: Uri, destination: File
    ): Boolean {
        withContext(Dispatchers.IO) {
            caller.contentResolver.openInputStream(sourceImage)?.use { input ->
                FileOutputStream(destination).use { output ->
                    input.copyTo(output)
                }
            }
        }

        return true
    }

// </editor-fold>
}