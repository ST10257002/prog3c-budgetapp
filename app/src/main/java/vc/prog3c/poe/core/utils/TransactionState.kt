package vc.prog3c.poe.utils

sealed class TransactionState {
    object Idle : TransactionState()
    object Loading : TransactionState()
    data class Success(val message: String = "Success") : TransactionState()
    data class Error(val message: String) : TransactionState()
}
