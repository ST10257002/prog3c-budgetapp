package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import vc.prog3c.poe.core.models.SignUpCredentials
import vc.prog3c.poe.core.utils.Blogger
import vc.prog3c.poe.databinding.ActivitySignUpBinding
import vc.prog3c.poe.ui.viewmodels.SignUpUiState
import vc.prog3c.poe.ui.viewmodels.SignUpViewModel

class SignUpActivity : AppCompatActivity(), View.OnClickListener {


    private lateinit var vBinds: ActivitySignUpBinding
    private lateinit var vModel: SignUpViewModel

    companion object {
        private const val TAG = "SignUpActivity"
    }


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


    private fun observeViewModel() = vModel.uiState.observe(this) { state ->
        when (state) {
            is SignUpUiState.Success -> navigateToDashboard()
            is SignUpUiState.Failure -> {
                Blogger.e(TAG, state.message)
                Toast.makeText(
                    this, state.message, Toast.LENGTH_SHORT
                ).show()
            }

            is SignUpUiState.Loading -> {}
            else -> {}
        }
    }


    // --- Internals


    private fun tryAuthenticateCredentials() {
        val credentials = SignUpCredentials(
            name = vBinds.etNameFirst.text.toString().trim(),
            surname = vBinds.etNameFinal.text.toString().trim(),
            usermail = vBinds.etUserMail.text.toString().trim(),
            username = vBinds.etUsername.text.toString().trim(),
            defaultPassword = vBinds.etDefaultPassword.text.toString().trim(),
            confirmPassword = vBinds.etConfirmPassword.text.toString().trim()
        )

        vModel.signUp(credentials)
    }


    private fun navigateToSignIn() {
        startActivity(Intent(this, SignInActivity::class.java))
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
            vBinds.btSignUp.id -> tryAuthenticateCredentials()
            vBinds.btSignIn.id -> navigateToSignIn()
        }
    }


    private fun setupClickListeners() {
        vBinds.btSignUp.setOnClickListener(this)
        vBinds.btSignIn.setOnClickListener(this)
    }


    // --- UI


    private fun setupBindings() {
        vBinds = ActivitySignUpBinding.inflate(layoutInflater)
    }


    private fun setupLayoutUi() {
        setContentView(vBinds.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(vBinds.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
