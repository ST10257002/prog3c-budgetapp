package vc.prog3c.poe.core.models

import android.net.Uri

/**
 * Result from a image file service operation.
 */
sealed class ImageResult {
    data class Success(val fileUri: Uri) : ImageResult()
    data class Blocked(val message: Throwable) : ImageResult()
    data class Failure(val message: Throwable) : ImageResult()
}