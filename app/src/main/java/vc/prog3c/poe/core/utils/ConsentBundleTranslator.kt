package vc.prog3c.poe.core.utils

import vc.prog3c.poe.core.models.ConsentBundle

/**
 * Translator to bidirectionally convert consents and permissions.
 *
 * @author ST10257002
 */
object ConsentBundleTranslator {

    /**
     * Convert the consent bundle to a collection of permission strings.
     *
     * @author ST10257002
     */
    fun toAndroid(bundle: ConsentBundle): Array<String> {
        return when (bundle) {
            ConsentBundle.CameraAccess -> ConsentVersionHelper.getCameraPermissionCode()
            ConsentBundle.ImageLibraryAccess -> ConsentVersionHelper.getImagePermissionCodes()
        }
    }
}