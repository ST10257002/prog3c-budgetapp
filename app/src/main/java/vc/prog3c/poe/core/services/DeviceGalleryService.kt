package vc.prog3c.poe.core.services

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vc.prog3c.poe.core.models.ImageResult
import vc.prog3c.poe.core.models.ImageResult.Blocked
import vc.prog3c.poe.core.models.ImageResult.Failure
import vc.prog3c.poe.core.models.ImageResult.Success
import vc.prog3c.poe.core.utils.ImageFileUtil

/**
 * Service to select an image from the device gallery.
 *
 * @param caller The reference to the Fragment or Activity that is calling the
 *        abstract service. This is necessary for functions using Android File
 *        APIs that require a valid context to be called from.
 *
 * @author ST10257002
 */
class DeviceGalleryService(
    caller: FragmentActivity
) : DeviceImageService(caller) {
// <editor-fold desc="Fields">


    private lateinit var galleryLauncher: ActivityResultLauncher<String>


// </editor-fold>
// <editor-fold desc="Service Methods">


    /**
     * Invokes the device gallery image picker.
     *
     * @author ST10257002
     */
    fun launchPicker() {
        galleryLauncher.launch(MULTIMEDIA_TYPE)
    }


    // @InheritDoc
    override fun registerForLauncherResult(
        callback: (ImageResult) -> Unit
    ) {
        this.callback = callback
        galleryLauncher = caller.registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri == null) {
                callback(Blocked(Exception(BLOCKED_MESSAGE)))
            } else {
                caller.lifecycleScope.launch {
                    val directory = createImageFile()
                    val completed = ImageFileUtil.copyUriToDirectory(
                        caller, uri, directory
                    )

                    withContext(Dispatchers.Main) {
                        if (completed) {
                            callback(Success(directory.toUri()))
                        } else {
                            callback(Failure(Exception(FAILURE_MESSAGE)))
                        }
                    }
                }
            }
        }
    }


// </editor-fold>
// <editor-fold desc="Constants">


    private companion object {
        const val BLOCKED_MESSAGE = "No image was selected"
        const val FAILURE_MESSAGE = "Failed to copy image to directory"
        const val MULTIMEDIA_TYPE = "image/*"
    }


// </editor-fold>
}