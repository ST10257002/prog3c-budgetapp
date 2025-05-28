package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import vc.prog3c.poe.core.utils.SeedData
import vc.prog3c.poe.data.models.User
import vc.prog3c.poe.data.services.FirestoreService
import java.util.regex.Pattern

class SignUpViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableLiveData<SignUpUiState>(SignUpUiState.Default)
    val uiState: LiveData<SignUpUiState> = _uiState

    fun signUp(name: String, email: String, password: String, confirmPassword: String) {
        if (!validateInput(name, email, password, confirmPassword)) return

        _uiState.value = SignUpUiState.Loading

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    _uiState.value = SignUpUiState.Failure("Sign-up failed: ${task.exception?.message}")
                    return@addOnCompleteListener
                }

                val userId = auth.currentUser?.uid.orEmpty()
                if (userId.isBlank()) {
                    _uiState.value = SignUpUiState.Failure("Failed to retrieve user ID")
                    return@addOnCompleteListener
                }

                val newUser = User(id = userId, uid = userId, name = name, email = email)

                viewModelScope.launch {
                    val result = FirestoreService.users.addUser(newUser)
                    if (result.isSuccess) {
                        SeedData.seedTestData(userId)
                        _uiState.postValue(SignUpUiState.Success)
                    } else {
                        _uiState.postValue(SignUpUiState.Failure("Firestore user creation failed"))
                    }
                }
            }
    }

    private fun validateInput(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        return when {
            name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                _uiState.value = SignUpUiState.Failure("All fields are required")
                false
            }
            !isValidEmail(email) -> {
                _uiState.value = SignUpUiState.Failure("Invalid email address")
                false
            }
            password != confirmPassword -> {
                _uiState.value = SignUpUiState.Failure("Passwords do not match")
                false
            }
            !isValidPassword(password) -> {
                _uiState.value = SignUpUiState.Failure("Password must be at least 6 characters")
                false
            }
            else -> true
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val pattern = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
            Pattern.CASE_INSENSITIVE
        )
        return pattern.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean = password.length >= 6
}
