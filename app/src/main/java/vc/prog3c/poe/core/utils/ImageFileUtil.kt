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
    private const val TAG = "ImageFileUtil"

    /**
     * A simple date format to generate uniquely timestamped filenames.
     * 
     * @reference Simple Date Format - https://developer.android.com/reference/java/text/SimpleDateFormat
     *
     * @see SimpleDateFormat
     * @author ST10257002
     */
    private val timestampFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    
    // --- Utilities

    
    /**
     * Creates and locates the private storage directory of the application.
     * 
     * @reference Android File Object - https://developer.android.com/reference/java/io/File
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
                Blogger.i(
                    TAG, "Created storage directory: $this"
                )
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
     * @reference Content Resolver - https://stackoverflow.com/questions/52743934/using-contentresolver-and-openinputstream-android
     * @reference Output Streaming - https://medium.com/@sujitpanda/file-read-write-with-kotlin-io-31eff564dfe3
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
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            caller.contentResolver.openInputStream(sourceImage)?.use { input ->
                FileOutputStream(destination).use { output ->
                    input.copyTo(output)
                }
            }

            Blogger.i(
                TAG, "Copied image file from  $sourceImage to $destination"
            )

            true
        } catch (e: IOException) {
            Blogger.e(
                TAG, "Failed to copy image file from $sourceImage to $destination", e
            )

            false
        }
    }
}