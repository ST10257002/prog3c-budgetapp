package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import vc.prog3c.poe.R
import vc.prog3c.poe.databinding.ActivityLoginBinding
import vc.prog3c.poe.ui.viewmodels.LoginViewModel

class LoginView : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        setupLoginButton()
        setupRegisterButton()
        setupBypassButton()
        observeViewModel()
    }

    private fun setupLoginButton() {
        binding.loginButton.setOnClickListener {
            val email = binding.userNameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // TODO: Backend Implementation Required
                // 1. Implement Firebase Authentication:
                //    - Use Firebase Auth for email/password authentication
                //    - Add email verification
                //    - Implement password reset functionality
                //    - Add rate limiting for failed attempts
                // 2. User Session Management:
                //    - Store user session in SharedPreferences
                //    - Implement token refresh mechanism
                //    - Handle session expiration
                // 3. Security:
                //    - Add input validation
                //    - Implement secure password storage
                //    - Add CAPTCHA for multiple failed attempts
                if (viewModel.login(email, password)) {
                    Toast.makeText(this, R.string.login_successful, Toast.LENGTH_SHORT).show()
                    navigateToNextScreen()
                } else {
                    Toast.makeText(this, viewModel.error.value, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRegisterButton() {
        binding.registerTextView.setOnClickListener {
            // TODO: Backend Implementation Required
            // 1. Registration Flow:
            //    - Implement email verification
            //    - Add phone number verification
            //    - Create user profile in Firestore
            //    - Set up initial user preferences
            startActivity(Intent(this, RegisterView::class.java))
        }
    }

    private fun setupBypassButton() {
        binding.bypassLogin.setOnClickListener {
            // TODO: Backend Implementation Required
            // 1. Guest User Setup:
            //    - Create temporary guest user in Firestore
            //    - Set up guest user preferences
            //    - Initialize guest user data
            //    - Set expiration for guest data
            viewModel.bypassLogin()
            // For guest login, go directly to dashboard
            startActivity(Intent(this, DashboardView::class.java))
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoggedIn.observe(this) { isLoggedIn ->
            if (isLoggedIn) {
                navigateToNextScreen()
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToNextScreen() {
        //the value should be true but ill change it later
        val intent = if (viewModel.isProfileComplete.value == false) {
            Intent(this, DashboardView::class.java)
        } else {
            Intent(this, CompleteProfileView::class.java)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

private fun LoginViewModel.setProfileComplete(bool: Boolean) {}
 