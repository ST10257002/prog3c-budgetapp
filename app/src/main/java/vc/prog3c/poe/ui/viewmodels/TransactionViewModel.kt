package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import vc.prog3c.poe.data.repository.TransactionRepository

/**
 * Unified ViewModel for both income and expense transactions.
 * All CRUD and filtering operations go through this single VM.
 */
class TransactionViewModel : ViewModel() {

    private val repo = TransactionRepository()

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    // full list loaded from Firestore
    private var _allTransactions = listOf<Transaction>()

    private val _totalIncome = MutableLiveData<Double>()
    val totalIncome: LiveData<Double> = _totalIncome

    private val _totalExpenses = MutableLiveData<Double>()
    val totalExpenses: LiveData<Double> = _totalExpenses

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /**
     * Load all transactions for an account (null = no-op or all accounts behavior).
     */
    fun loadTransactions(accountId: String?) {
        _isLoading.value = true
        _error.value = null

        if (accountId == null) {
            // No account specified: clear lists
            _allTransactions = emptyList()
            _transactions.postValue(_allTransactions)
            updateTotals(_allTransactions)
            _isLoading.postValue(false)
            return
        }

        repo.getTransactions(accountId, null) { list ->
            _allTransactions = list
            _transactions.postValue(list)
            updateTotals(list)
            _isLoading.postValue(false)
        }
    }

    /** Refresh the current account's transactions. */
    fun refreshTransactions(accountId: String?) = loadTransactions(accountId)

    /**
     * Filter the loaded transactions by type (INCOME or EXPENSE).
     */
    fun filterTransactionsByType(type: TransactionType) {
        val filtered = _allTransactions.filter { it.type == type }
        _transactions.postValue(filtered)
        updateTotals(filtered)
    }

    /**
     * Add a new transaction (income or expense).
     */
    fun addTransaction(transaction: Transaction) {
        _isLoading.value = true
        repo.addTransaction(transaction) { success ->
            if (success) {
                refreshTransactions(transaction.accountId)
            } else {
                _error.postValue("Failed to add transaction.")
            }
            _isLoading.postValue(false)
        }
    }

    /**
     * Update an existing transaction.
     */
    fun updateTransaction(transaction: Transaction) {
        _isLoading.value = true
        repo.updateTransaction(transaction) { success ->
            if (success) {
                refreshTransactions(transaction.accountId)
            } else {
                _error.postValue("Failed to update transaction.")
            }
            _isLoading.postValue(false)
        }
    }

    /**
     * Delete a transaction by ID.
     */
    fun deleteTransaction(transactionId: String, accountId: String) {
        _isLoading.value = true
        repo.deleteTransaction(transactionId, accountId) { success ->
            if (success) {
                refreshTransactions(accountId)
            } else {
                _error.postValue("Failed to delete transaction.")
            }
            _isLoading.postValue(false)
        }
    }

    /**
     * Recalculate total income and expense from a given list.
     */
    private fun updateTotals(list: List<Transaction>) {
        _totalIncome.value = list
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }

        _totalExpenses.value = list
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
    }
}
