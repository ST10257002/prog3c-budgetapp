package vc.prog3c.poe.core.utils

import android.Manifest

/**
 * Helper class for device version checks.
 */
object ConsentVersionHelper {

    fun getCameraPermissionCode(): Array<String> = when {
        AndroidVersionHelper.isDeviceVersionAtLeastApi33Tiramisu() -> {
            arrayOf(Manifest.permission.CAMERA)
        }

        AndroidVersionHelper.isDeviceVersionAtLeastApi29Q() -> {
            arrayOf(
                Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

        else -> {
            arrayOf(Manifest.permission.CAMERA)
        }
    }


    fun getImagePermissionCodes(): Array<String> = when {
        AndroidVersionHelper.isDeviceVersionAtLeastApi33Tiramisu() -> {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        }

        AndroidVersionHelper.isDeviceVersionAtLeastApi32SV2() -> {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        AndroidVersionHelper.isDeviceVersionAtLeastApi31SV1() -> {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        AndroidVersionHelper.isDeviceVersionAtLeastApi30R() -> {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

        AndroidVersionHelper.isDeviceVersionAtLeastApi29Q() -> {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        else -> {
            throw UnsupportedOperationException(UNSUPPORTED_EXCEPTION)
        }
    }


    // --- Constants


    private const val UNSUPPORTED_EXCEPTION = "Caught request made for an unsupported version."
}