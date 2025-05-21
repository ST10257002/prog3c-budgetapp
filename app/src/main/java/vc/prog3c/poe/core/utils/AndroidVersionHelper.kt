package vc.prog3c.poe.core.utils

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

/**
 * Helper class for performing device version checks against specific versions
 * of the Android operating system.
 *
 * @author ST10257002
 */
object AndroidVersionHelper {

//<editor-fold desc="Functions">


    /**
     * Check if the active system version is at least the specified version.
     *
     * @param apiLevel The minimum API version to check against.
     *
     * @see Build.VERSION_CODES
     * @see Build.VERSION
     *
     * @return True if the system version matches the constraint.
     * @author ST10257002
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.BASE)
    fun isDeviceVersionAtLeast(apiLevel: Int): Boolean = (Build.VERSION.SDK_INT >= apiLevel)


    /**
     * Get the the API level of the Android operating system used on the device.
     *
     * @see Build.VERSION.SDK_INT
     *
     * @return The device API level.
     * @author ST10257002
     */
    fun getDeviceApiLevel(): Int = Build.VERSION.SDK_INT


//</editor-fold>
//<editor-fold desc="Helpers">


    /**
     * Check if the active system version is at least Android 13 (API level 33).
     *
     * @return True if the system version matches the constraint.
     * @author ST10257002
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
    fun isDeviceVersionAtLeastApi33Tiramisu(): Boolean =
        isDeviceVersionAtLeast(apiLevel = Build.VERSION_CODES.TIRAMISU)


    /**
     * Check if the active system version is at least Android 12 (API level 32).
     *
     * **Note:**
     * This version of Android has two related API versions, ensure that you use
     * the correct one in your codebase.
     *
     * @return True if the system version matches the constraint.
     * @author ST10257002
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S_V2)
    fun isDeviceVersionAtLeastApi32SV2(): Boolean =
        isDeviceVersionAtLeast(apiLevel = Build.VERSION_CODES.S_V2)


    /**
     * Check if the active system version is at least Android 12 (API level 31).
     *
     * **Note:**
     * This version of Android has two related API versions, ensure that you use
     * the correct one in your codebase.
     *
     * @return True if the system version matches the constraint.
     * @author ST10257002
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
    fun isDeviceVersionAtLeastApi31SV1(): Boolean =
        isDeviceVersionAtLeast(apiLevel = Build.VERSION_CODES.S)


    /**
     * Check if the active system version is at least Android 11 (API level 30).
     *
     * @return True if the system version matches the constraint.
     * @author ST10257002
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.R)
    fun isDeviceVersionAtLeastApi30R(): Boolean =
        isDeviceVersionAtLeast(apiLevel = Build.VERSION_CODES.R)


    /**
     * Check if the active system version is at least Android 10 ( API level 29).
     *
     * @return True if the system version matches the constraint.
     * @author ST10257002
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
    fun isDeviceVersionAtLeastApi29Q(): Boolean =
        isDeviceVersionAtLeast(apiLevel = Build.VERSION_CODES.Q)


//</editor-fold>
}