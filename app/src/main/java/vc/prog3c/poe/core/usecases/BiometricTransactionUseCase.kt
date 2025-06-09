package vc.prog3c.poe.core.usecases

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import vc.prog3c.poe.core.models.BiometricUiHost

class BiometricTransactionUseCase(
    private val caller: FragmentActivity, private val uiHost: BiometricUiHost
) {

    /**
     * @reference Android Biometrics - https://www.youtube.com/watch?v=_dCRQ9wta-I
     */
    fun execute() {
        if (!isBiometricAvailable(caller)) {
            uiHost.onBiometricsException(
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
                "Device does not have any biometrics sensors"
            )
            
            return
        }
        
        val executor = ContextCompat.getMainExecutor(caller)
        val dialogUi = BiometricPrompt(
            caller, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult
                    ) {
                        super.onAuthenticationSucceeded(result)
                        uiHost.onBiometricsSucceeded()
                    }


                    override fun onAuthenticationError(
                        errorCode: Int, errString: CharSequence
                    ) {
                        super.onAuthenticationError(errorCode, errString)
                        uiHost.onBiometricsDismissed()
                    }
                }
            )
        
        dialogUi.authenticate(
            constructBuilder()
        )
    }

    
    // --- Internals
    
    
    private fun constructBuilder() : BiometricPrompt.PromptInfo {
        val builder = BiometricPrompt.PromptInfo.Builder().apply {
            setNegativeButtonText("Cancel")
        }

        uiHost.onShowBiometrics(builder) // override from ui
        return builder.build()
    }


    /**
     * @reference Android Credentials - https://www.youtube.com/watch?v=_dCRQ9wta-I
     */
    private fun isBiometricAvailable(
        context: Context
    ): Boolean {
        val queryResult = BiometricManager.from(context)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        return queryResult == BiometricManager.BIOMETRIC_SUCCESS
    }
}