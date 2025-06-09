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
import vc.prog3c.poe.core.utils.Blogger
import vc.prog3c.poe.core.utils.ImageFileUtil

/**
 * Service to select an image from the device gallery.
 * 
 * @reference FragmentActivity - https://developer.android.com/reference/androidx/fragment/app/FragmentActivity
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
    
    private lateinit var galleryLauncher: ActivityResultLauncher<String>


    /**
     * Invokes the device gallery image picker.
     *
     * @author ST10257002
     */
    fun launchPicker() {
        Blogger.i(
            TAG, "Started launching the picker"
        )

        galleryLauncher.launch(MULTIMEDIA_TYPE)
    }


    /**
     * @reference Activity Result - https://developer.android.com/training/basics/intents/result
     * @reference Activity Result from Gallery - https://developer.android.com/reference/androidx/activity/result/contract/ActivityResultContracts.GetContent
     */
    override fun registerForLauncherResult(
        callback: (ImageResult) -> Unit
    ) {
        Blogger.i(
            TAG, "Started registering for launcher result"
        )

        this.callback = callback
        galleryLauncher = caller.registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri == null) {
                val e = Exception(BLOCKED_MESSAGE)
                Blogger.d(TAG, BLOCKED_MESSAGE)
                callback(Blocked(e))
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
                            val e = Exception(FAILURE_MESSAGE)
                            Blogger.e(TAG, FAILURE_MESSAGE, e)
                            callback(Failure(e))
                        }
                    }
                }
            }
        }
    }


    private companion object {
        const val TAG = "DeviceGalleryService"
        const val BLOCKED_MESSAGE = "No image was selected"
        const val FAILURE_MESSAGE = "Failed to copy image to directory"
        const val MULTIMEDIA_TYPE = "image/*"
    }
}