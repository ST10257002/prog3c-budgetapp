package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import vc.prog3c.poe.core.utils.SeedData
import vc.prog3c.poe.data.models.User
import vc.prog3c.poe.data.services.FirestoreService
import java.util.regex.Pattern

class SignUpViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val _uiState = MutableLiveData<SignUpUiState>()
    val uiState: LiveData<SignUpUiState> = _uiState

    fun signUp(name: String, email: String, password: String, confirmPassword: String) {

        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _uiState.value = SignUpUiState.Failure("All fields must be filled")
            return
        }
        if (!isValidEmail(email)) {
            _uiState.value = SignUpUiState.Failure("Invalid email address")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = SignUpUiState.Failure("Passwords do not match")
            return
        }
        if (!isValidPassword(password)) {
            _uiState.value = SignUpUiState.Failure("Password must be at least 6 characters")
            return
        }


        _uiState.value = SignUpUiState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId == null) {
                        _uiState.value = SignUpUiState.Failure("Failed to retrieve user ID")
                        return@addOnCompleteListener
                    }


                    val user = User(id = userId, name = name, email = email)
                    FirestoreService.users.addUser(user) { success ->
                        if (success) {

                            SeedData.seedTestData(userId)

                            _uiState.value = SignUpUiState.Success
                        } else {
                            _uiState.value = SignUpUiState.Failure("Firestore user creation failed")
                        }
                    }
                } else {
                    _uiState.value = SignUpUiState.Failure("Sign-up failed: ${task.exception?.message}")
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

    private fun isValidPassword(password: String): Boolean = password.length >= 6
}
