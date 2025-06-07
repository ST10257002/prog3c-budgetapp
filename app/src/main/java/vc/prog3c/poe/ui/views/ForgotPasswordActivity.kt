package vc.prog3c.poe.ui.views

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import vc.prog3c.poe.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binds: ActivityForgotPasswordBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBindings()
        setupLayoutUi()
        setupClickListeners()
    }

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

    private fun setupClickListeners() {
        binds.btResetPassword.setOnClickListener(this)
        binds.btBackToLogin.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            binds.btResetPassword.id -> sendPasswordResetEmail()
            binds.btBackToLogin.id -> finish()
        }
    }

    private fun sendPasswordResetEmail() {
        val email = binds.etEmail.text.toString().trim()
        
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
            return
        }

        binds.loadingIndicator.visibility = View.VISIBLE
        binds.btResetPassword.isEnabled = false

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                binds.loadingIndicator.visibility = View.GONE
                binds.btResetPassword.isEnabled = true

                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Password reset email sent. Please check your inbox.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Failed to send reset email: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
} 