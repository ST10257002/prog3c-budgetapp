package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import vc.prog3c.poe.core.models.SignUpCredentials
import vc.prog3c.poe.core.services.AuthService
import vc.prog3c.poe.core.utils.Blogger
import vc.prog3c.poe.data.models.User
import vc.prog3c.poe.data.services.FirestoreService
import vc.prog3c.poe.ui.viewmodels.SignUpUiState.Failure
import vc.prog3c.poe.ui.viewmodels.SignUpUiState.Loading
import vc.prog3c.poe.ui.viewmodels.SignUpUiState.Success

class SignUpViewModel(
    private val authService: AuthService = AuthService()
) : ViewModel() {
    companion object {
        private const val TAG = "SignUpViewModel"
    }

    // --- Fields

    private val _uiState = MutableLiveData<SignUpUiState>()
    val uiState: LiveData<SignUpUiState> = _uiState

    // --- Activity Functions

    /**
     * Uses the [AuthService] to register a new user in Firebase.
     */
    fun signUp(
        credentials: SignUpCredentials
    ) = viewModelScope.launch {
        credentials.getValidationErrors()?.let {
            _uiState.value = Failure(it)
            return@launch
        }

        _uiState.value = Loading

        runCatching {
            withTimeout(10000) {
                authService.signUpAsync(
                    usermail = credentials.usermail, // Asserted by now
                    password = credentials.defaultPassword // Validated
                )
            }
        }.apply {
            onSuccess { user ->
                linkUserToDatabase(user.uid, credentials)
            }

            onFailure { throwable ->
                Blogger.d(TAG, "Sign-up failed: ${throwable.message}")
                _uiState.value = Failure("Unexpected error during sign-up")
            }
        }
    }

    private fun linkUserToDatabase(
        userId: String, credentials: SignUpCredentials
    ) {
        val user = User(
            id = userId,
            email = credentials.usermail,
            name = credentials.name,
            surname = credentials.surname
        )

        FirestoreService.user.addUser(user) { operationSuccessful ->
            when (operationSuccessful) {
                true -> onDatabaseLinkSuccess()
                else -> viewModelScope.launch {
                    onDatabaseLinkFailure()
                    _uiState.value = Failure("Unexpected error during sign-up")
                }
            }
        }
    }

    // --- Callbacks

    private fun onDatabaseLinkSuccess() {
        Blogger.i(TAG, "Successfully linked the auth user in the database")
        _uiState.value = Success
    }

    private suspend fun onDatabaseLinkFailure() {
        runCatching {
            withTimeout(5000) {
                authService.deleteCurrentUserAsync()
            }
        }.apply {
            onSuccess {
                Blogger.i(TAG, "Successfully deleted the auth user")
            }

            onFailure { throwable ->
                Blogger.i(TAG, "Failed to delete the auth user: ${throwable.message}")
            }
        }
    }
}
