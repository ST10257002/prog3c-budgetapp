package vc.prog3c.poe.ui.viewmodels

sealed interface ForgotPasswordUiState {
    object Success : ForgotPasswordUiState
    object Loading : ForgotPasswordUiState
    data class Failure(val message: String) : ForgotPasswordUiState
}