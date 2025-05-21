package vc.prog3c.poe.core.services

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import vc.prog3c.poe.core.models.ImageResult
import vc.prog3c.poe.core.models.ImageResult.Blocked
import vc.prog3c.poe.core.models.ImageResult.Failure
import vc.prog3c.poe.core.models.ImageResult.Success

/**
 * Service to capture an image from the device camera.
 *
 * @param caller The reference to the Fragment or Activity that is calling the
 *        abstract service. This is necessary for functions using Android File
 *        APIs that require a valid context to be called from.
 *
 * @author ST10257002
 */
class DeviceCaptureService(
    caller: FragmentActivity
) : DeviceImageService(caller) {
// <editor-fold desc="Fields">


    private lateinit var captureLauncher: ActivityResultLauncher<Uri>
    private var imageUri: Uri? = null


// </editor-fold>
// <editor-fold desc="Service Methods">


    /**
     * Invokes the device camera with the file provider.
     *
     * @author ST10257002
     */
    fun launchCamera() {
        val file = createImageFile()
        imageUri = FileProvider.getUriForFile(
            caller, "${caller.packageName}.fileprovider", file
        )

        imageUri?.let {
            captureLauncher.launch(it)
        } ?: callback?.invoke(Failure(Exception(LAUNCHER_FAILURE_MESSAGE)))
    }


    // @InheritDoc
    override fun registerForLauncherResult(
        callback: (ImageResult) -> Unit
    ) {
        this.callback = callback
        captureLauncher = caller.registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            var result: ImageResult = if (success.not()) {
                Blocked(Exception(BLOCKED_MESSAGE))
            } else {
                when (imageUri != null) {
                    true -> Success(imageUri!!)
                    else -> Failure(Exception(REGISTER_FAILURE_MESSAGE))
                }
            }

            callback(result)
        }
    }


// </editor-fold>
// <editor-fold desc="Constants">


    private companion object {
        const val BLOCKED_MESSAGE = "Image capture was cancelled"
        const val LAUNCHER_FAILURE_MESSAGE = "Failed to create the image file"
        const val REGISTER_FAILURE_MESSAGE = "Something went wrong with the camera"
    }


// </editor-fold>
}