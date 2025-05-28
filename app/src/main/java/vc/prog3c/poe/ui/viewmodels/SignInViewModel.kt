// ✅ SignInViewModel.kt
package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Pattern

class SignInViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val _uiState = MutableLiveData<SignInUiState>(SignInUiState.Default)
    val uiState: LiveData<SignInUiState> = _uiState

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = SignInUiState.Failure("Email and password are required")
            return
        }

        if (!isValidEmail(email)) {
            _uiState.value = SignInUiState.Failure("Invalid email format")
            return
        }

        _uiState.value = SignInUiState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _uiState.value = if (task.isSuccessful) {
                    SignInUiState.Success
                } else {
                    SignInUiState.Failure("Authentication failed: ${task.exception?.message}")
                }
            }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
            Pattern.CASE_INSENSITIVE
        )
        return emailPattern.matcher(email).matches()
    }
}
