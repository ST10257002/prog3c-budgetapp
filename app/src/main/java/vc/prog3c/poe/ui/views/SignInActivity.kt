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

    private lateinit var binding: ActivitySignInBinding
    private lateinit var viewModel: SignInViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[SignInViewModel::class.java]

        setupListeners()
        observeUiState()
        authenticateWithBiometrics()
    }

    private fun observeUiState() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is SignInUiState.Success -> navigateToDashboard()
                is SignInUiState.Failure -> showToast(state.message)
                is SignInUiState.Loading -> {
                    // Optional: show loading indicator here
                }
                else -> Unit
            }
        }
    }

    private fun attemptCredentialLogin() {
        val email = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        viewModel.signIn(email, password)
    }

    private fun authenticateWithBiometrics() {
        BiometricTransactionUseCase(this, this).execute()
    }

    private fun setupListeners() {
        binding.loginButton.setOnClickListener(this)
        binding.registerTextView.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            binding.loginButton.id -> attemptCredentialLogin()
            binding.registerTextView.id -> navigateToSignUp()
        }
    }

    private fun navigateToDashboard() {
        Intent(this, DashboardView::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(this)
        }
        finish()
    }

    private fun navigateToSignUp() {
        startActivity(Intent(this, SignUpActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // --- BiometricUiHost implementations ---

    override fun onShowBiometrics(uiBuilder: BiometricPrompt.PromptInfo.Builder) {
        uiBuilder.apply {
            setTitle("Login with fingerprint")
            setDescription("Use your fingerprint to log in")
            setNegativeButtonText("Use password instead")
        }
    }

    override fun onBiometricsSucceeded() {
        showToast("Biometrics successful")
        // You could navigate or auto-login here if credentials are cached
    }

    override fun onBiometricsDismissed() {
        showToast("Biometric login canceled")
    }

    override fun onBiometricsException(code: Int, message: String) {
        Blogger.d(this::class.java.simpleName, "Biometric error ($code): $message")
    }
}
