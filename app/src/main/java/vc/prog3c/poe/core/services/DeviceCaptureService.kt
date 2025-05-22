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
import vc.prog3c.poe.core.utils.Blogger

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
        Blogger.i(
            TAG, "Started launching the camera"
        )

        val file = createImageFile()
        imageUri = FileProvider.getUriForFile(
            caller, "${caller.packageName}.fileprovider", file
        )

        imageUri?.let {
            Blogger.i(
                TAG, "Launching camera with file provider: $imageUri"
            )

            captureLauncher.launch(it)
        } ?: {
            val e = Exception(LAUNCHER_FAILURE_MESSAGE)
            Blogger.e(TAG, LAUNCHER_FAILURE_MESSAGE, e)
            callback?.invoke(Failure(e))
        }
    }


    // @InheritDoc
    override fun registerForLauncherResult(
        callback: (ImageResult) -> Unit
    ) {
        Blogger.i(
            TAG, "Started registering for launcher result"
        )

        this.callback = callback
        captureLauncher = caller.registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            var result: ImageResult = if (success.not()) {
                val e = Exception(BLOCKED_MESSAGE)
                Blogger.d(TAG, BLOCKED_MESSAGE)
                Blocked(e)
            } else {
                when (imageUri != null) {
                    true -> {
                        Blogger.i(
                            TAG, "Successfully captured image from camera: $imageUri"
                        )

                        Success(imageUri!!)
                    }

                    else -> {
                        val e = Exception(REGISTER_FAILURE_MESSAGE)
                        Blogger.e(TAG, REGISTER_FAILURE_MESSAGE, e)
                        Failure(e)
                    }
                }
            }

            callback(result)
        }
    }


// </editor-fold>
// <editor-fold desc="Constants">


    private companion object {
        const val TAG = "DeviceCaptureService"
        const val BLOCKED_MESSAGE = "Image capture was cancelled"
        const val LAUNCHER_FAILURE_MESSAGE = "Failed to create the image file"
        const val REGISTER_FAILURE_MESSAGE = "Something went wrong with the camera"
    }


// </editor-fold>
}