package vc.prog3c.poe.ui.viewmodels

sealed interface SignUpUiState {
    object Success : SignUpUiState
    object Loading : SignUpUiState
    data class Failure(val message: String) : SignUpUiState
}