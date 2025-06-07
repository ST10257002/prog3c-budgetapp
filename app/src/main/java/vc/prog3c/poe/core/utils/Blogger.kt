package vc.prog3c.poe.core.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Logger to synchronise reports to Logcat and Crashlytics.
 */
object Blogger {
    private var crashlytics: FirebaseCrashlytics? = null

    init {
        try {
            crashlytics = FirebaseCrashlytics.getInstance()
        } catch (e: Exception) {
            Log.e("Blogger", "Failed to initialize Firebase Crashlytics", e)
        }
    }

    
    // --- Logging
    

    /**
     * Log an information message.
     */
    fun i(tag: String, message: String) {
        Log.i(tag, message)
        crashlytics?.log("[I][${tag}]: $message.")
    }

    /**
     * Log a warning message.
     */
    fun w(tag: String, message: String) {
        Log.w(tag, message)
        crashlytics?.log("[W][${tag}]: $message.")
    }

    /**
     * Log a debug message.
     */
    fun d(tag: String, message: String) {
        Log.d(tag, message)
        crashlytics?.log("[D][${tag}]: $message.")
    }

    /**
     * Log an error message.
     *
     * **Note:** This function will record a non-fatal exception in Crashlytics
     * if a throwable is provided as a parameter. Otherwise, it will be treated
     * as any other error log.
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        crashlytics?.log("[E][${tag}]: $message.")
        if (throwable != null) {
            crashlytics?.recordException(throwable)
        }
    }
}