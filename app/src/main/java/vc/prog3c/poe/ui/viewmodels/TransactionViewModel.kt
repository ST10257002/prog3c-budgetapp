package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import vc.prog3c.poe.data.repository.TransactionRepository
import vc.prog3c.poe.utils.TransactionState

class TransactionViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _totalIncome = MutableLiveData<Double>()
    val totalIncome: LiveData<Double> = _totalIncome

    private val _totalExpenses = MutableLiveData<Double>()
    val totalExpenses: LiveData<Double> = _totalExpenses

    private val _state = MutableLiveData<TransactionState>(TransactionState.Idle)
    val state: LiveData<TransactionState> = _state

    fun getCurrentUserId(): String = auth.currentUser?.uid ?: ""

    fun loadTransactions(accountId: String, filter: TransactionType? = null) {
        _state.value = TransactionState.Loading
        viewModelScope.launch {
            repository.getTransactions(accountId, filter).onSuccess { txns ->
                _transactions.value = txns
                calculateTotals(txns)
                _state.value = TransactionState.Success()
            }.onFailure {
                _state.value = TransactionState.Error(it.message ?: "Failed to fetch transactions")
            }
        }
    }

    fun addTransaction(transaction: Transaction) {
        _state.value = TransactionState.Loading
        viewModelScope.launch {
            repository.addTransaction(transaction).onSuccess {
                _state.value = TransactionState.Success("Transaction added")
                loadTransactions(transaction.accountId)
            }.onFailure {
                _state.value = TransactionState.Error(it.message ?: "Failed to add")
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        _state.value = TransactionState.Loading
        viewModelScope.launch {
            repository.updateTransaction(transaction).onSuccess {
                _state.value = TransactionState.Success("Transaction updated")
                loadTransactions(transaction.accountId)
            }.onFailure {
                _state.value = TransactionState.Error(it.message ?: "Failed to update")
            }
        }
    }

    fun deleteTransaction(accountId: String, transactionId: String) {
        _state.value = TransactionState.Loading
        viewModelScope.launch {
            repository.deleteTransaction(accountId, transactionId).onSuccess {
                _state.value = TransactionState.Success("Transaction deleted")
                loadTransactions(accountId)
            }.onFailure {
                _state.value = TransactionState.Error(it.message ?: "Failed to delete")
            }
        }
    }

    private fun calculateTotals(txns: List<Transaction>) {
        _totalIncome.value = txns.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        _totalExpenses.value = txns.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }
}
