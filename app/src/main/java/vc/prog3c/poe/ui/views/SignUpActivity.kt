package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import vc.prog3c.poe.R
import vc.prog3c.poe.databinding.ActivitySignUpBinding
import vc.prog3c.poe.ui.viewmodels.AuthViewModel

class SignUpActivity : AppCompatActivity(), View.OnClickListener {


    private lateinit var vBinds: ActivitySignUpBinding
    private lateinit var vModel: AuthViewModel


    // --- Lifecycle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBindings()
        setupLayoutUi()
        setupClickListeners()

        vModel = ViewModelProvider(this)[AuthViewModel::class.java]

        observeViewModel()
    }


    // --- ViewModel


    private fun observeViewModel() {
        vModel.isLoggedIn.observe(this) { isLoggedIn ->
            if (isLoggedIn) {
                startMainActivity()
            }
        }

        vModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(
                    this, it, Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    // --- Activity


    private fun setupRegisterButton() {
        val firstName = vBinds.firstNameEditText.text.toString()
        val lastName = vBinds.lastNameEditText.text.toString()
        val username = vBinds.etUsername.text.toString()
        val email = vBinds.emailEditText.text.toString()
        val password = vBinds.etPassword.text.toString()
        val confirmPassword = vBinds.confirmPasswordEditText.text.toString()

        if (arrayOf(firstName, lastName, username, email, password, confirmPassword).any {
                it.isEmpty()
            }) {
            Toast.makeText(
                this, "Inputs cannot be empty", Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(
                this, "The passwords don't match", Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (vModel.register("$firstName $lastName", email, password, confirmPassword)) {
            Toast.makeText(
                this, R.string.registration_successful, Toast.LENGTH_SHORT
            ).show()
            startMainActivity()
        } else {
            Toast.makeText(this, vModel.error.value, Toast.LENGTH_SHORT).show()
        }
    }


    private fun setupLoginButton() {
        startActivity(
            Intent(this, SignInActivity::class.java)
        )
        finish()
    }


    private fun startMainActivity() {
        val intent = Intent(this, CompleteProfileView::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


    // --- Event Handlers


    override fun onClick(view: View?) {
        when (view?.id) {
            vBinds.registerButton.id -> setupRegisterButton()
            vBinds.loginButton.id -> setupLoginButton()
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