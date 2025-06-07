package vc.prog3c.poe.core.models

import android.net.Uri

/**
 * Result from a image file service operation.
 */
sealed class ImageResult {
    data class Success(val fileUri: Uri) : ImageResult() // Success -> returns the file URI
    data class Blocked(val message: Throwable) : ImageResult() // User cancelled the selection/capture (not important)
    data class Failure(val message: Throwable) : ImageResult() // Something went wrong and threw a throwable/exception
}