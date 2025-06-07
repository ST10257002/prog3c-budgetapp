package vc.prog3c.poe.ui.views

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import vc.prog3c.poe.core.utils.Blogger
import vc.prog3c.poe.databinding.ActivityForgotPasswordBinding
import vc.prog3c.poe.ui.viewmodels.ForgotPasswordUiState
import vc.prog3c.poe.ui.viewmodels.ForgotPasswordViewModel

class ForgotPasswordActivity : AppCompatActivity(), View.OnClickListener {
    companion object {
        private const val TAG = "ForgotPasswordActivity"
    }

    private lateinit var binds: ActivityForgotPasswordBinding
    private lateinit var model: ForgotPasswordViewModel

    // --- Lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBindings()
        setupLayoutUi()
        setupClickListeners()

        model = ViewModelProvider(this)[ForgotPasswordViewModel::class.java]
        
        observeViewModel()
    }

    // --- ViewModel

    private fun observeViewModel() = model.uiState.observe(this) { state ->
        when (state) {
            is ForgotPasswordUiState.Success -> {
                Toast.makeText(
                    this, "Password reset email sent", Toast.LENGTH_SHORT
                ).show()
                finish()
            }

            is ForgotPasswordUiState.Failure -> {
                Blogger.e(TAG, state.message)
                Toast.makeText(
                    this, state.message, Toast.LENGTH_SHORT
                ).show()

                binds.loadingIndicator.visibility = View.GONE
                binds.btResetPassword.isEnabled = true
            }

            is ForgotPasswordUiState.Loading -> {
                binds.loadingIndicator.visibility = View.VISIBLE
                binds.btResetPassword.isEnabled = false
            }
        }
    }

    // --- Internals

    private fun sendPasswordResetEmail() {
        val email = binds.etEmail.text.toString().trim()
        if (email.isEmpty()) {
            Toast.makeText(
                this, "Please enter your email address", Toast.LENGTH_SHORT
            ).show()
            return
        }

        model.sendPasswordResetEmail(email)
    }

    // --- Event Handlers

    override fun onClick(view: View?) {
        when (view?.id) {
            binds.btResetPassword.id -> sendPasswordResetEmail()
            binds.btBackToLogin.id -> finish()
        }
    }

    private fun setupClickListeners() {
        binds.btResetPassword.setOnClickListener(this)
        binds.btBackToLogin.setOnClickListener(this)
    }

    // --- UI

    private fun setupBindings() {
        binds = ActivityForgotPasswordBinding.inflate(layoutInflater)
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