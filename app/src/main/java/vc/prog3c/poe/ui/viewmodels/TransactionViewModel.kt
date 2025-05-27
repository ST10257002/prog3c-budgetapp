package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import java.util.Date

class TransactionViewModel : ViewModel() {
    // TODO: Replace with Firestore implementation
    // - Create Firestore collection for transactions
    // - Implement real-time listeners for transaction updates
    // - Add offline persistence support
    // - Implement data synchronization
    // - Add error handling for network issues

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _filteredTransactions = MutableLiveData<List<Transaction>>()
    val filteredTransactions: LiveData<List<Transaction>> = _filteredTransactions

    private val _totalIncome = MutableLiveData<Double>()
    val totalIncome: LiveData<Double> = _totalIncome

    private val _totalExpenses = MutableLiveData<Double>()
    val totalExpenses: LiveData<Double> = _totalExpenses

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private var currentAccountId: String? = null

    init {
        // Initial load might not have accountId, will load all or handle accordingly
        // loadInitialData()
    }

    fun loadTransactions(accountId: String? = null) {
        currentAccountId = accountId
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // TODO: Backend Implementation Required
                // - Fetch transactions from Firestore, potentially filtered by accountId
                // - Handle offline state
                // - Implement error handling
                delay(1000) // Simulate network delay
                loadTestData(accountId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to load transactions: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadTestData(accountId: String? = null) {
        // Test data for transactions
        val allTransactions = listOf(
            Transaction(
                id = "1",
                userId = "user1",
                accountId = "account1",
                amount = 25000.0,
                type = TransactionType.INCOME,
                category = "Salary",
                description = "Monthly salary",
                date = Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000)
            ),
            Transaction(
                id = "2",
                userId = "user1",
                 accountId = "account1",
                amount = 5000.0,
                type = TransactionType.EXPENSE,
                category = "Groceries",
                description = "Monthly groceries",
                date = Date(System.currentTimeMillis() - 6 * 24 * 60 * 60 * 1000)
            ),
            Transaction(
                id = "3",
                userId = "user1",
                 accountId = "account2",
                amount = 5000.0,
                type = TransactionType.INCOME,
                category = "Freelance",
                description = "Web development project",
                date = Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000)
            ),
            Transaction(
                id = "4",
                userId = "user1",
                 accountId = "account2",
                amount = 2000.0,
                type = TransactionType.EXPENSE,
                category = "Transport",
                description = "Fuel and maintenance",
                date = Date(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000)
            )
            // Add more test data with different account IDs if needed
        )

        val transactionsToDisplay = if (accountId != null) {
            allTransactions.filter { it.accountId == accountId } // Use accountId for filtering
        } else {
            allTransactions
        }

        _transactions.value = transactionsToDisplay.sortedByDescending { it.date }

        updateTotals(transactionsToDisplay)
    }

    private fun updateFilteredTransactions() {
        // This function might be redundant now, as filtering is done in loadTestData
        _filteredTransactions.value = _transactions.value //?.sortedByDescending { it.date }
    }

    private fun updateTotals(transactions: List<Transaction>) {
        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        _totalIncome.value = totalIncome
        _totalExpenses.value = totalExpenses
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                // TODO: Implement Firestore Transaction Creation
                // 1. Add new document to 'transactions' subcollection with:
                //    - transactionId (auto-generated)
                //    - userId (from current user)
                //    - accountId (if applicable)
                //    - type (INCOME/EXPENSE)
                //    - amount
                //    - description
                //    - category
                //    - date
                //    - photos (array of URLs)
                // 2. Update user's total balance and potentially account balance
                // 3. Update category totals
                // 4. Handle photo uploads to Firebase Storage
                // 5. Implement transaction validation
                // 6. Add error handling for:
                //    - Invalid amounts
                //    - Network issues
                //    - Storage quota exceeded
                val currentList = _transactions.value?.toMutableList() ?: mutableListOf()
                currentList.add(transaction)
                _transactions.value = currentList
                updateTotals(currentList)
                _saveSuccess.value = true
            } catch (e: Exception) {
                _error.value = "Failed to add transaction: ${e.message}"
                _saveSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        // TODO: Implement Firestore Transaction Update
        // 1. Update existing document in 'transactions' subcollection
        // 2. Recalculate totals if amount or type changed (and account balance)
        // 3. Handle photo updates
        // 4. Implement optimistic updates
        // 5. Add conflict resolution
        // 6. Handle offline updates
        val currentList = _transactions.value?.toMutableList() ?: return
        val index = currentList.indexOfFirst { it.id == transaction.id }
        if (index != -1) {
            currentList[index] = transaction
            _transactions.value = currentList // This might need adjustment
            // updateFilteredTransactions() // Redundant
            updateTotals(currentList)
        }
    }

    fun deleteTransaction(transactionId: String) {
        // TODO: Implement Firestore Transaction Deletion
        // 1. Delete document from 'transactions' subcollection
        // 2. Update user's total balance and potentially account balance
        // 3. Update category totals
        // 4. Delete associated photos from Storage
        // 5. Implement soft delete for transaction history
        // 6. Handle offline deletion
        val currentList = _transactions.value?.toMutableList() ?: return
        currentList.removeIf { it.id == transactionId }
        _transactions.value = currentList // This might need adjustment
        // updateFilteredTransactions() // Redundant
        updateTotals(currentList)
    }

    fun filterTransactionsByType(type: TransactionType, accountId: String? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // TODO: Backend Implementation Required
                // - Implement filtering in Firestore query by type and accountId
                // - Handle offline state
                // - Implement error handling
                delay(500) // Simulate network delay

                val allTransactions = _transactions.value ?: emptyList()

                val filteredByType = when (type) {
                    TransactionType.ALL -> allTransactions
                    TransactionType.INCOME -> allTransactions.filter { it.type == TransactionType.INCOME }
                    TransactionType.EXPENSE -> allTransactions.filter { it.type == TransactionType.EXPENSE }
                }

                // Apply account filtering if accountId is provided
                val finalFilteredList = if (accountId != null) {
                    filteredByType.filter { it.accountId == accountId } // Use accountId for filtering
                } else {
                    filteredByType
                }

                _transactions.value = finalFilteredList.sortedByDescending { it.date }
                updateTotals(finalFilteredList)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to filter transactions: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getTransactionsByDateRange(startDate: Date, endDate: Date) {
        // TODO: Implement Firestore Date Range Queries, potentially filtered by accountId
        // 1. Query 'transactions' subcollection where:
        //    - date >= startDate
        //    - date <= endDate
        //    - potentially accountId == accountId
        // 2. Implement efficient date range queries
        // 3. Add date-based caching
        // 4. Handle timezone issues
        // 5. Implement date range validation
    }

    fun getTransactionsByCategory(category: String) {
        // TODO: Implement Firestore Category Queries, potentially filtered by accountId
        // 1. Query 'transactions' subcollection where:
        //    - category == category
        //    - potentially accountId == accountId
        // 2. Add category-based caching
        // 3. Implement category statistics
        // 4. Handle category updates
        // 5. Add category validation
    }

    fun refreshTransactions(accountId: String? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // TODO: Backend Implementation Required
                // - Implement real-time refresh from Firestore, potentially filtered by accountId
                // - Handle offline state
                delay(1000) // Simulate network delay
                loadTestData(accountId) // Reload data including filtering
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to refresh transactions: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun retryLastOperation() {
        // For now, just reload the transactions
        loadTransactions(currentAccountId)
    }

} 