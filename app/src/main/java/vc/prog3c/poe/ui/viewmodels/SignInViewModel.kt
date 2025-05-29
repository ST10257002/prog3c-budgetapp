package vc.prog3c.poe.ui.viewmodels

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import vc.prog3c.poe.core.models.SignInCredentials
import vc.prog3c.poe.core.services.AuthService
import vc.prog3c.poe.core.utils.Blogger
import vc.prog3c.poe.data.services.FirestoreService

class SignInViewModel(
    private val authService: AuthService = AuthService()
) : ViewModel() {
    companion object {
        private const val TAG = "SignInViewModel"
    }


    // --- Fields


    private val _uiState = MutableLiveData<SignInUiState>()
    val uiState: LiveData<SignInUiState> = _uiState


    // --- Activity Functions


    /**
     * Uses the [AuthService] to authenticate the returning user.
     */
    fun signIn(
        credentials: SignInCredentials
    ) = viewModelScope.launch {
        credentials.getValidationErrors()?.let {
            _uiState.value = SignInUiState.Failure(it)
            return@launch
        }

        _uiState.value = SignInUiState.Loading

        runCatching {
            withTimeout(10000) {
                authService.signInAsync(credentials.identity, credentials.password)
            }
        }.apply {
            onSuccess { user ->
                Blogger.d(TAG, "Sign-in for user ${user.uid} success")
                _uiState.value = SignInUiState.Success
            }
            
            onFailure { throwable ->
                Blogger.d(TAG, "Sign-up failed: ${throwable.message}")
                _uiState.value = SignInUiState.Failure("Unexpected error during sign-in")
            }
        }
    }


    fun canAutoLoginUser(): Boolean = authService.isUserSignedIn()
    
    
    fun tryAutoLoginUser() {
        when (canAutoLoginUser()) {
            true -> _uiState.value = SignInUiState.Success
            else -> throw IllegalStateException("Authenticated user is not cached")
        }
    }
}
