package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import vc.prog3c.poe.R
import vc.prog3c.poe.core.models.BiometricUiHost
import vc.prog3c.poe.core.models.SignInCredentials
import vc.prog3c.poe.core.usecases.BiometricTransactionUseCase
import vc.prog3c.poe.core.utils.Blogger
import vc.prog3c.poe.core.utils.Notifier
import vc.prog3c.poe.databinding.ActivitySignInBinding
import vc.prog3c.poe.ui.viewmodels.SignInUiState
import vc.prog3c.poe.ui.viewmodels.SignInViewModel
/**
 * @reference Android EditText Input Validation: https://developer.android.com/guide/topics/ui/controls/text
 * @reference Android View Binding with Buttons: https://developer.android.com/topic/libraries/view-binding
 */

class SignInActivity : AppCompatActivity(), View.OnClickListener, BiometricUiHost {
    companion object {
        private const val TAG = "SignInActivity"
    }

    private lateinit var binds: ActivitySignInBinding
    private lateinit var model: SignInViewModel

    // --- Lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBindings()
        setupLayoutUi()
        setupClickListeners()

        model = ViewModelProvider(this)[SignInViewModel::class.java]

        observeViewModel()

        if (model.canAutoLoginUser()) {
            tryAuthenticateUsingBiometrics()
        }
    }

    // --- ViewModel

    private fun observeViewModel() = model.uiState.observe(this) { state ->
        when (state) {
            is SignInUiState.Success -> navigateToNextScreen()
            is SignInUiState.Failure -> {
                Blogger.e(TAG, state.message)
                Toast.makeText(
                    this, state.message, Toast.LENGTH_SHORT
                ).show()
            }

            is SignInUiState.Loading -> {}
            else -> {}
        }
    }

    // --- Internals

    private fun tryAuthenticateWithCredentials() {
        val credentials = SignInCredentials(
            identity = binds.etUsername.text.toString().trim(),
            password = binds.etPassword.text.toString().trim()
        )

        model.signIn(credentials)
    }

    private fun tryAuthenticateUsingBiometrics() {
        BiometricTransactionUseCase(
            caller = this, uiHost = this
        ).execute()
    }

    private fun navigateToNextScreen() {
        Notifier.bottomSnackbar(binds.root, "🎉 Login successful!", this)

        // Delay navigation by ~1 second to show the snackbar
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, DashboardView::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }, 1000)
    }

    // --- Event Handlers

    override fun onClick(view: View?) {
        when (view?.id) {
            binds.btSignIn.id -> tryAuthenticateWithCredentials()
            binds.tvSignUp.id -> startActivity(Intent(this, SignUpActivity::class.java))
            binds.btIconFingerprint.id -> {
                if (model.canAutoLoginUser()) {
                    tryAuthenticateUsingBiometrics()
                } else {
                    Toast.makeText(
                        this, "Sign in with your credentials first", Toast.LENGTH_SHORT
                    ).show()
                }
            }

            binds.tvForgotPassword.id -> startActivity(
                Intent(
                    this, ForgotPasswordActivity::class.java
                )
            )
        }
    }

    private fun setupClickListeners() {
        binds.btSignIn.setOnClickListener(this)
        binds.tvSignUp.setOnClickListener(this)
        binds.btIconFingerprint.setOnClickListener(this)
        binds.tvForgotPassword.setOnClickListener(this)
    }

    // --- Biometrics

    override fun onShowBiometrics(
        uiBuilder: BiometricPrompt.PromptInfo.Builder
    ) {
        uiBuilder.apply {
            setTitle(getString(R.string.biometrics_scope_title))
            setDescription(getString(R.string.biometrics_scope_description))
            setNegativeButtonText(getString(R.string.biometrics_scope_on_negative))
        }
    }

    override fun onBiometricsSucceeded() {
        Toast.makeText(
            this, "Biometrics Succeeded", Toast.LENGTH_SHORT
        ).show()

        model.tryAutoLoginUser()
    }

    override fun onBiometricsDismissed() {}

    override fun onBiometricsException(
        code: Int, message: String
    ) {
        Blogger.e(TAG, message)
    }

    // --- UI

    private fun setupBindings() {
        binds = ActivitySignInBinding.inflate(layoutInflater)
    }

    private fun setupLayoutUi() {
        setContentView(binds.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binds.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
