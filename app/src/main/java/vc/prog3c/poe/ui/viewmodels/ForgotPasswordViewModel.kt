package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import vc.prog3c.poe.core.services.AuthService
import vc.prog3c.poe.core.utils.Blogger
import vc.prog3c.poe.ui.viewmodels.ForgotPasswordUiState.Failure
import vc.prog3c.poe.ui.viewmodels.ForgotPasswordUiState.Loading
import vc.prog3c.poe.ui.viewmodels.ForgotPasswordUiState.Success

class ForgotPasswordViewModel(
    private val authService: AuthService = AuthService()
) : ViewModel() {
    companion object {
        private const val TAG = "ForgotPasswordViewModel"
    }

    // --- Fields

    private val _uiState = MutableLiveData<ForgotPasswordUiState>()
    val uiState: LiveData<ForgotPasswordUiState> = _uiState

    // --- Activity Functions

    /**
     * Uses the [AuthService] to send a password reset email to the given address.
     */
    fun sendPasswordResetEmail(
        email: String
    ) = viewModelScope.launch {
        Blogger.d(TAG, "User requested password reset email")
        _uiState.value = Loading

        runCatching {
            withTimeout(5000) {
                authService.sendPasswordResetEmailAsync(email)
            }
        }.apply {
            onSuccess {
                Blogger.d(TAG, "Password reset sent to: $email")
                _uiState.value = Success
            }

            onFailure { throwable ->
                Blogger.d(TAG, "Password reset failed: ${throwable.message}")
                _uiState.value = Failure("Could not send reset email")
            }
        }
    }
}