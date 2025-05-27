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

class SignInActivity : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener,
    BiometricUiHost {


    private lateinit var vBinds: ActivitySignInBinding
    private lateinit var vModel: SignInViewModel


    // --- Lifecycle


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setupBindings()
        setupLayoutUi()
        setupClickListeners()

        vModel = ViewModelProvider(this)[SignInViewModel::class.java]

        observeViewModel()
        tryAuthenticateUsingBiometrics()
    }


    // --- ViewModel


    private fun observeViewModel() {
        vModel.uiState.observe(this) { state ->
            when (state) {
                is SignInUiState.Success -> {
                    navigateToNextScreen()
                }

                is SignInUiState.Failure -> {
                    Toast.makeText(
                        this, state.message, Toast.LENGTH_SHORT
                    ).show()
                }

                is SignInUiState.Loading -> {}

                else -> {
                    // Default behaviour
                }
            }
        }
    }


    // --- Activity


    private fun tryAuthenticateWithCredentials() {
        val identity = vBinds.etUsername.text.toString().trim()
        val password = vBinds.etPassword.text.toString().trim()

        vModel.signIn(
            identity, password
        )
    }


    private fun tryAuthenticateUsingBiometrics() {
        vModel.getCurrentUser()?.let {
            BiometricTransactionUseCase(
                caller = this, uiHost = this
            ).execute()
        }
    }


    private fun navigateToNextScreen() {
        val intent = Intent(this, DashboardView::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


    // --- Event Handlers


    override fun onClick(view: View?) {
        when (view?.id) {
            vBinds.loginButton.id -> tryAuthenticateWithCredentials()

            vBinds.registerTextView.id -> {
                startActivity(
                    Intent(this, SignUpActivity::class.java)
                )
            }
        }
    }


    override fun onLongClick(
        view: View?
    ): Boolean {
        when (view?.id) {
            vBinds.loginButton.id -> {
                Toast.makeText(
                    this, "Bypassing authentication", Toast.LENGTH_SHORT
                ).show()

                vModel.bypassLogin()

                // here add in testing for testData function
                //vc.prog3c.poe.utils.TestData.runFirestoreTest()
                vc.prog3c.poe.utils.TestRead.runIncomeTest()
            }
        }

        return true
    }


    private fun setupClickListeners() {
        vBinds.loginButton.setOnClickListener(this)
        vBinds.loginButton.setOnLongClickListener(this)
        vBinds.registerTextView.setOnClickListener(this)
    }


    // --- Biometrics


    override fun onShowBiometrics(
        uiBuilder: BiometricPrompt.PromptInfo.Builder
    ) {
        uiBuilder.apply {
            setTitle("Login with fingerprint")
            setDescription(
                "We have found a user already logged in. Use your fingerprint to autofill your account details and jump straight into the app."
            )
            setNegativeButtonText(
                "Use another method"
            )
        }
    }


    override fun onBiometricsSucceeded() {
        Toast.makeText(
            this, "Biometrics Succeeded", Toast.LENGTH_SHORT
        ).show()

        vModel.bypassLogin()
    }


    override fun onBiometricsDismissed() {
        Toast.makeText(
            this, "Biometrics Dismissed", Toast.LENGTH_SHORT
        ).show()
    }


    override fun onBiometricsException(
        code: Int, message: String
    ) {
        Blogger.d(this::class.java.simpleName, message)
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