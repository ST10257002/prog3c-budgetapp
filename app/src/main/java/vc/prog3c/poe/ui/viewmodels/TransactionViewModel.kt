package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType

/**
 * Unified ViewModel for both income and expense transactions.
 * All CRUD and filtering operations go through this single VM.
 */
class TransactionViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    // full list loaded from Firestore
    private var _allTransactions = listOf<Transaction>()

    private val _totalIncome = MutableLiveData<Double>()
    val totalIncome: LiveData<Double> = _totalIncome

    private val _totalExpenses = MutableLiveData<Double>()
    val totalExpenses: LiveData<Double> = _totalExpenses

    private val _transactionState = MutableLiveData<TransactionState>()
    val transactionState: LiveData<TransactionState> = _transactionState

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun getCurrentUserId(): String = auth.currentUser?.uid ?: ""

    /**
     * Load all transactions for an account (null = no-op or all accounts behavior).
     */
    fun loadTransactions(accountId: String?) {
        if (accountId == null) return
        _transactionState.value = TransactionState.Loading
        
        viewModelScope.launch {
            try {
                firestore.collection("transactions")
                    .whereEqualTo("accountId", accountId)
                    .get()
                    .addOnSuccessListener { documents ->
                        val transactionList = documents.mapNotNull { it.toObject(Transaction::class.java) }
                        _transactions.value = transactionList
                        calculateTotals(transactionList)
                        _transactionState.value = TransactionState.Success()
                    }
                    .addOnFailureListener { e ->
                        _transactionState.value = TransactionState.Error(e.message ?: "Failed to load transactions")
                    }
            } catch (e: Exception) {
                _transactionState.value = TransactionState.Error(e.message ?: "An error occurred")
            }
        }
    }

    /** Refresh the current account's transactions. */
    fun refreshTransactions(accountId: String?) = loadTransactions(accountId)

    /**
     * Filter the loaded transactions by type (INCOME or EXPENSE).
     */
    fun filterTransactionsByType(type: TransactionType) {
        val currentList = _transactions.value ?: return
        _transactions.value = currentList.filter { it.type == type }
        calculateTotals(currentList.filter { it.type == type })
    }

    /**
     * Add a new transaction (income or expense).
     */
    fun addTransaction(transaction: Transaction) {
        _isLoading.value = true
        _error.value = null

        _transactionState.value = TransactionState.Loading
        
        viewModelScope.launch {
            try {
                firestore.collection("transactions")
                    .document(transaction.id)
                    .set(transaction)
                    .addOnSuccessListener {
                        _transactionState.value = TransactionState.Success()
                        loadTransactions(transaction.accountId)
                    }
                    .addOnFailureListener { e ->
                        _transactionState.value = TransactionState.Error(e.message ?: "Failed to add transaction")
                    }
            } catch (e: Exception) {
                _transactionState.value = TransactionState.Error(e.message ?: "An error occurred")
            }
        }
    }

    /**
     * Update an existing transaction.
     */
    fun updateTransaction(transaction: Transaction) {
        _isLoading.value = true
        _error.value = null

        _transactionState.value = TransactionState.Loading
        
        viewModelScope.launch {
            try {
                firestore.collection("transactions")
                    .document(transaction.id)
                    .set(transaction)
                    .addOnSuccessListener {
                        _transactionState.value = TransactionState.Success()
                        loadTransactions(transaction.accountId)
                    }
                    .addOnFailureListener { e ->
                        _transactionState.value = TransactionState.Error(e.message ?: "Failed to update transaction")
                    }
            } catch (e: Exception) {
                _transactionState.value = TransactionState.Error(e.message ?: "An error occurred")
            }
        }
    }

    /**
     * Delete a transaction by ID.
     */
    fun deleteTransaction(transactionId: String, accountId: String) {
        _isLoading.value = true
        _error.value = null

        _transactionState.value = TransactionState.Loading
        
        viewModelScope.launch {
            try {
                firestore.collection("transactions")
                    .document(transactionId)
                    .delete()
                    .addOnSuccessListener {
                        _transactionState.value = TransactionState.Success()
                        loadTransactions(accountId)
                    }
                    .addOnFailureListener { e ->
                        _transactionState.value = TransactionState.Error(e.message ?: "Failed to delete transaction")
                    }
            } catch (e: Exception) {
                _transactionState.value = TransactionState.Error(e.message ?: "An error occurred")
            }
        }
    }

    private fun calculateTotals(transactions: List<Transaction>) {
        _totalIncome.value = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
        
        _totalExpenses.value = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
    }
}
