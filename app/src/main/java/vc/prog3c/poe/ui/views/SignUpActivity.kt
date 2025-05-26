package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import vc.prog3c.poe.databinding.ActivitySignUpBinding
import vc.prog3c.poe.ui.viewmodels.SignUpUiState
import vc.prog3c.poe.ui.viewmodels.SignUpViewModel

class SignUpActivity : AppCompatActivity(), View.OnClickListener {


    private lateinit var vBinds: ActivitySignUpBinding
    private lateinit var vModel: SignUpViewModel


    // --- Lifecycle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBindings()
        setupLayoutUi()
        setupClickListeners()

        vModel = ViewModelProvider(this)[SignUpViewModel::class.java]

        observeViewModel()
    }


    // --- ViewModel


    private fun observeViewModel() {
        vModel.uiState.observe(this) { state ->
            when (state) {
                is SignUpUiState.Success -> {
                    navigateToDashboard()
                }

                is SignUpUiState.Failure -> {
                    Toast.makeText(
                        this, state.message, Toast.LENGTH_SHORT
                    ).show()
                }

                is SignUpUiState.Loading -> {}

                else -> {
                    // Default behaviour
                }
            }
        }
    }


    // --- Activity


    private fun tryAuthenticateCredentials() {
        val name = vBinds.firstNameEditText.text.toString().trim()
        val surname = vBinds.lastNameEditText.text.toString().trim()
        val username = vBinds.etUsername.text.toString().trim()
        val usermail = vBinds.emailEditText.text.toString().trim()
        val defaultPassword = vBinds.etPassword.text.toString().trim()
        val confirmPassword = vBinds.confirmPasswordEditText.text.toString().trim()

        vModel.signUp(
            username, defaultPassword, confirmPassword, usermail, name, surname
        )
    }


    private fun navigateToSignIn() {
        startActivity(
            Intent(this, SignInActivity::class.java)
        )
        finish()
    }


    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardView::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


    // --- Event Handlers


    override fun onClick(view: View?) {
        when (view?.id) {
            vBinds.registerButton.id -> tryAuthenticateCredentials()
            vBinds.loginButton.id -> navigateToSignIn()
        }
    }


    private fun setupClickListeners() {
        vBinds.registerButton.setOnClickListener(this)
        vBinds.loginButton.setOnClickListener(this)
    }


    // --- UI


    private fun setupBindings() {
        vBinds = ActivitySignUpBinding.inflate(layoutInflater)
    }


    private fun setupLayoutUi() {
        setContentView(vBinds.root)
        enableEdgeToEdge()
    }
} 