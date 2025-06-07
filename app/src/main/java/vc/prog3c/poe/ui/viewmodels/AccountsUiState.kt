package vc.prog3c.poe.ui.viewmodels

import vc.prog3c.poe.data.models.Account

sealed interface AccountsUiState {
    object Default : AccountsUiState
    object Loading : AccountsUiState
    
    data class Updated(
        val accounts: List<Account>? = null,
        val netWorth: Double? = null
    ) : AccountsUiState
    
    data class Failure(val message: String) : AccountsUiState
}