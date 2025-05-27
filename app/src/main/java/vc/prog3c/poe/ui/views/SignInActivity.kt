package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.ViewModelProvider
import vc.prog3c.poe.core.models.BiometricUiHost
import vc.prog3c.poe.core.usecases.BiometricTransactionUseCase
import vc.prog3c.poe.core.utils.Blogger
import vc.prog3c.poe.databinding.ActivitySignInBinding
import vc.prog3c.poe.ui.viewmodels.SignInUiState
import vc.prog3c.poe.ui.viewmodels.SignInViewModel

class SignInActivity : AppCompatActivity(), View.OnClickListener, BiometricUiHost {

    private lateinit var vBinds: ActivitySignInBinding
    private lateinit var vModel: SignInViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBindings()
        setupLayoutUi()
        setupClickListeners()

        vModel = ViewModelProvider(this)[SignInViewModel::class.java]

        observeViewModel()
        tryAuthenticateUsingBiometrics()
    }

    private fun observeViewModel() {
        vModel.uiState.observe(this) { state ->
            when (state) {
                is SignInUiState.Success -> navigateToNextScreen()
                is SignInUiState.Failure -> Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                is SignInUiState.Loading -> {} // Optional: show loading spinner
                else -> {}
            }
        }
    }

    private fun tryAuthenticateWithCredentials() {
        val identity = vBinds.etUsername.text.toString().trim()
        val password = vBinds.etPassword.text.toString().trim()
        vModel.signIn(identity, password)
    }

    private fun tryAuthenticateUsingBiometrics() {
        // Optional: only call if a user is already cached (you can add auth check if needed)
        BiometricTransactionUseCase(this, this).execute()
    }

    private fun navigateToNextScreen() {
        startActivity(Intent(this, DashboardView::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            vBinds.loginButton.id -> tryAuthenticateWithCredentials()
            vBinds.registerTextView.id -> {
                startActivity(Intent(this, SignUpActivity::class.java))
            }
        }
    }

    private fun setupClickListeners() {
        vBinds.loginButton.setOnClickListener(this)
        vBinds.registerTextView.setOnClickListener(this)
    }

    // --- Biometrics ---
    override fun onShowBiometrics(uiBuilder: BiometricPrompt.PromptInfo.Builder) {
        uiBuilder.apply {
            setTitle("Login with fingerprint")
            setDescription("Use your fingerprint to login quickly")
            setNegativeButtonText("Use password instead")
        }
    }

    override fun onBiometricsSucceeded() {
        Toast.makeText(this, "Biometrics Succeeded", Toast.LENGTH_SHORT).show()
        // Optional: auto-login if you cache credentials
    }

    override fun onBiometricsDismissed() {
        Toast.makeText(this, "Biometrics Dismissed", Toast.LENGTH_SHORT).show()
    }

    override fun onBiometricsException(code: Int, message: String) {
        Blogger.d(this::class.java.simpleName, message)
    }

    private fun setupBindings() {
        vBinds = ActivitySignInBinding.inflate(layoutInflater)
    }

    private fun setupLayoutUi() {
        setContentView(vBinds.root)
        enableEdgeToEdge()
    }
}
