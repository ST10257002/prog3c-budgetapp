package vc.prog3c.poe.ui.viewmodels

sealed class TransactionUpsertState {
    object Loading : TransactionUpsertState()
    data class Success(val message: String) : TransactionUpsertState()
    data class Error(val message: String) : TransactionUpsertState()
}
