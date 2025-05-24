package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import vc.prog3c.poe.R
import vc.prog3c.poe.databinding.ActivitySignInBinding
import vc.prog3c.poe.ui.viewmodels.AuthViewModel

class SignInActivity : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener {


    private lateinit var vBinds: ActivitySignInBinding
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
                navigateToNextScreen()
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


    private fun tryAuthenticateCredentials() {
        val username = vBinds.etUsername.text.toString()
        val password = vBinds.etPassword.text.toString()

        if (username.isEmpty() && password.isEmpty()) {
            Toast.makeText(
                this, "Inputs cannot be empty", Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (vModel.login(username, password)) {
            Toast.makeText(
                this, R.string.login_successful, Toast.LENGTH_SHORT
            ).show()

            navigateToNextScreen()
        } else {
            Toast.makeText(
                this, vModel.error.value, Toast.LENGTH_SHORT
            ).show()
        }
    }


    private fun navigateToNextScreen() {
        val intent = if (vModel.isProfileComplete.value == false) {
            Intent(this, DashboardView::class.java)
        } else {
            Intent(this, CompleteProfileView::class.java)
        }

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


    // --- Event Handlers


    override fun onClick(view: View?) {
        when (view?.id) {
            vBinds.loginButton.id -> tryAuthenticateCredentials()

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
                startActivity(
                    Intent(this, DashboardView::class.java)
                )
                finish()
            }
        }
        
        return true
    }


    private fun setupClickListeners() {
        vBinds.loginButton.setOnClickListener(this)
        vBinds.loginButton.setOnLongClickListener(this)
        vBinds.registerTextView.setOnClickListener(this)
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