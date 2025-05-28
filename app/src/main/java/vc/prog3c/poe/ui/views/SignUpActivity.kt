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

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var viewModel: SignUpViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[SignUpViewModel::class.java]

        setupListeners()
        observeUiState()
    }

    private fun observeUiState() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is SignUpUiState.Success -> navigateToDashboard()
                is SignUpUiState.Failure -> showToast(state.message)
                is SignUpUiState.Loading -> {
                    // Optionally show a loading indicator
                }
                else -> Unit
            }
        }
    }

    private fun attemptSignUp() {
        val firstName = binding.firstNameEditText.text.toString().trim()
        val lastName = binding.lastNameEditText.text.toString().trim()
        val fullName = "$firstName $lastName"
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()

        viewModel.signUp(fullName, email, password, confirmPassword)
    }

    private fun setupListeners() {
        binding.registerButton.setOnClickListener(this)
        binding.loginButton.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            binding.registerButton.id -> attemptSignUp()
            binding.loginButton.id -> navigateToSignIn()
        }
    }

    private fun navigateToDashboard() {
        Intent(this, DashboardView::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(this)
        }
        finish()
    }

    private fun navigateToSignIn() {
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
