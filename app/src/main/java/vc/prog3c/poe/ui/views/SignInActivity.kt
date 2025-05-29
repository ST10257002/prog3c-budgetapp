package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.ViewModelProvider
import vc.prog3c.poe.R
import vc.prog3c.poe.core.models.BiometricUiHost
import vc.prog3c.poe.core.models.SignInCredentials
import vc.prog3c.poe.core.usecases.BiometricTransactionUseCase
import vc.prog3c.poe.core.utils.Blogger
import vc.prog3c.poe.databinding.ActivitySignInBinding
import vc.prog3c.poe.ui.viewmodels.SignInUiState
import vc.prog3c.poe.ui.viewmodels.SignInViewModel

class SignInActivity : AppCompatActivity(), View.OnClickListener, BiometricUiHost {


    private lateinit var vBinds: ActivitySignInBinding
    private lateinit var vModel: SignInViewModel

    companion object {
        private const val TAG = "SignInActivity"
    }


    // --- Lifecycle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBindings()
        setupLayoutUi()
        setupClickListeners()

        vModel = ViewModelProvider(this)[SignInViewModel::class.java]

        observeViewModel()

        if (vModel.canAutoLoginUser()) {
            tryAuthenticateUsingBiometrics()
        }
    }


    // --- ViewModel


    private fun observeViewModel() {
        vModel.uiState.observe(this) { state ->
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
    }


    // --- Internals


    private fun tryAuthenticateWithCredentials() {
        val credentials = SignInCredentials(
            identity = vBinds.etUsername.text.toString().trim(),
            password = vBinds.etPassword.text.toString().trim()
        )

        vModel.signIn(credentials)
    }


    private fun tryAuthenticateUsingBiometrics() {
        BiometricTransactionUseCase(
            caller = this, uiHost = this
        ).execute()
    }


    private fun navigateToNextScreen() {
        startActivity(Intent(this, DashboardView::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }


    // --- Event Handlers


    override fun onClick(view: View?) {
        when (view?.id) {
            vBinds.btSignIn.id -> tryAuthenticateWithCredentials()
            vBinds.tvSignUp.id -> {
                startActivity(Intent(this, SignUpActivity::class.java))
            }
        }
    }


    private fun setupClickListeners() {
        vBinds.btSignIn.setOnClickListener(this)
        vBinds.tvSignUp.setOnClickListener(this)
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

        vModel.tryAutoLoginUser()
    }


    override fun onBiometricsDismissed() {
        Toast.makeText(
            this, "Biometrics Dismissed", Toast.LENGTH_SHORT
        ).show()
    }


    override fun onBiometricsException(
        code: Int, message: String
    ) {
        Blogger.e(TAG, message)
    }


    // --- UI


    private fun setupBindings() {
        vBinds = ActivitySignInBinding.inflate(layoutInflater)
    }


    private fun setupLayoutUi() {
        setContentView(vBinds.root)
        enableEdgeToEdge()
    }
}
