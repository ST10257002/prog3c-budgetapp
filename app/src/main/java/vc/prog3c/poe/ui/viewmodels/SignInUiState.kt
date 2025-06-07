package vc.prog3c.poe.ui.viewmodels

sealed interface SignInUiState {
    object Success : SignInUiState
    object Loading : SignInUiState
    data class Failure(val message: String) : SignInUiState
}