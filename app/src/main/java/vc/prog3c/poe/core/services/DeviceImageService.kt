package vc.prog3c.poe.core.services

import androidx.fragment.app.FragmentActivity
import vc.prog3c.poe.core.models.ImageResult
import vc.prog3c.poe.core.utils.ImageFileUtil

/**
 * Abstract contract for services that store images on the device.
 *
 * @see [DeviceCaptureService]
 * @see [DeviceGalleryService]
 *
 * @param caller The reference to the Fragment or Activity that is calling the
 *        abstract service. This is necessary for functions using Android File
 *        APIs that require a valid context to be called from.
 *
 * @author ST10257002
 */
abstract class DeviceImageService(
    protected val caller: FragmentActivity
) {

    /**
     * Stores a reference to a callback function that will be invoked when an
     * abstract service operation completes.
     *
     * @see [registerForLauncherResult]
     * @author ST10257002
     */
    protected var callback: ((ImageResult) -> Unit)? = null


    /**
     * Delegates the responsibility of creating a new unique image file to the
     * [ImageFileUtil]. This method does not save the image to device storage,
     * it just prepares the directory for the image to be saved to, and informs
     * the abstract service implementation of the new file path.
     *
     * @author ST10257002
     */
    protected fun createImageFile() = ImageFileUtil.nameUniqueImageFile(caller)


    /**
     * Defines a contract between the UI implementation and the abstract service
     * implementation. The UI implementation will call this method to define how
     * the different outputs of this service will be handled, if at all. This
     * decouples business logic from the activity lifecycle.
     *
     * **Examples:**
     *
     * ```
     * // Handle the output in an activity
     * deviceImageService.registerForLauncherResult { result ->
     *     when (result) {
     *         is ImageResult.Success -> {
     *             // Get the actual image file URI on a successful event
     *             adapter.addImage(result.fileUri.toString())
     *         }
     *
     *         is ImageResult.Failure -> {
     *             Toast.makeText(
     *                 this, result.message.toString(), Toast.LENGTH_LONG
     *             ).show()
     *         }
     *
     *         is ImageResult.Blocked -> {
     *             Toast.makeText(
     *                 this, result.message.toString(), Toast.LENGTH_LONG
     *             ).show()
     *         }
     *     }
     * }
     * ```
     *
     * ```
     * // Handle only some events
     * deviceImageService.registerForLauncherResult { result ->
     *     if (result is ImageResult.Success) {
     *         // Get the actual image file URI on a successful event
     *         adapter.addImage(result.fileUri.toString())
     *     }
     * }
     * ```
     *
     * @return [ImageResult] describing the status of the operation.
     *         If the request was successful, an `ImageResult.Success` object is
     *         returned containing the file URI of the image. If the request was
     *         cancelled, a `ImageResult.Blocked` object is returned with a
     *         throwable as the message holder. If an error occurred with the IO
     *         aspect of the action, an `ImageResult.Failure` object is returned
     *         with a throwable as the message holder.
     * @author ST10257002
     */
    abstract fun registerForLauncherResult(callback: (ImageResult) -> Unit)
}