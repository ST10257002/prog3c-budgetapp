package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import vc.prog3c.poe.core.services.AuthService
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import java.util.*
import vc.prog3c.poe.data.models.SortOption
import vc.prog3c.poe.core.utils.CurrencyFormatter
import vc.prog3c.poe.data.services.AchievementEvaluator
import vc.prog3c.poe.ui.viewmodels.AchievementViewModel
import vc.prog3c.poe.data.models.Account
import vc.prog3c.poe.data.services.FirestoreService

/**
 * Unified ViewModel for both income and expense transactions.
 * All CRUD and filtering operations go through this single VM.
 *
 * @reference Android ViewModel Guide: https://developer.android.com/topic/libraries/architecture/viewmodel
 * @reference Firebase Firestore - Query Collections: https://firebase.google.com/docs/firestore/query-data/queries
 * @reference Firebase Firestore - Kotlin Coroutines Support: https://firebase.google.com/docs/firestore/query-data/get-data#kotlin+ktx
 * @reference LiveData Guide: https://developer.android.com/topic/libraries/architecture/livedata
 */

class TransactionViewModel(
    private val authService: AuthService = AuthService(),
    private val dataService: FirestoreService = FirestoreService
) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val transactionsCollection = firestore.collection("transactions")

    val accountName = MutableLiveData<String>()

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

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

    private val _singleTransaction = MutableLiveData<Transaction?>()
    val singleTransaction: LiveData<Transaction?> = _singleTransaction

    private val _transaction = MutableLiveData<Transaction>()
    val transaction: LiveData<Transaction> = _transaction

    private var allTransactions = listOf<Transaction>()
    private var currentFilter = "All"
    private var currentSort = SortOption.DATE_DESC

    private var _fullTransactionList: List<Transaction> = emptyList()

    lateinit var achievementViewModel: AchievementViewModel


    /**
     * Load all transactions for an account.
     */
    fun loadTransactions(accountId: String) {
        viewModelScope.launch {
            _transactionState.value = TransactionState.Loading
            try {
                val userId = authService.getCurrentUser()?.uid ?: return@launch

                // Load transactions
                val transactionList = firestore.collection("users")
                    .document(userId)
                    .collection("accounts")
                    .document(accountId)
                    .collection("transactions")
                    .get()
                    .await()
                    .toObjects(Transaction::class.java)

                allTransactions = transactionList
                applyFilterAndSort()
                _transactionState.value = TransactionState.Success()

                // Load accounts
                val accountsSnapshot = firestore.collection("users")
                    .document(userId)
                    .collection("accounts")
                    .get()
                    .await()

                val accounts = accountsSnapshot.toObjects(Account::class.java)

                // Load categories
                val categoriesSnapshot = firestore.collection("users")
                    .document(userId)
                    .collection("categories")
                    .get()
                    .await()

                val categories = categoriesSnapshot.toObjects(vc.prog3c.poe.data.models.Category::class.java)

                // Evaluate achievements
                evaluateAchievements(userId, accounts, categories)

            } catch (e: Exception) {
                _transactionState.value = TransactionState.Error(e.message ?: "Failed to load transactions")
            }
        }
    }


    fun loadAccountName(accountId: String) {
        dataService.account.getAccount(accountId) { result ->
            accountName.value = result?.name ?: "Unknown Account"
        }
    }



    fun refreshTransactions(accountId: String?) = loadTransactions(accountId ?: "")

    fun filterTransactions(source: String) {
        currentFilter = source
        applyFilterAndSort()
    }

    fun sortTransactions(sortOption: SortOption) {
        currentSort = sortOption
        applyFilterAndSort()
    }

    private fun applyFilterAndSort() {
        var filteredTransactions = when (currentFilter) {
            "Income" -> allTransactions.filter { it.type == TransactionType.INCOME }
            "Expense" -> allTransactions.filter { it.type == TransactionType.EXPENSE }
            else -> allTransactions
        }

        filteredTransactions = when (currentSort) {
            SortOption.DATE_DESC -> filteredTransactions.sortedByDescending { it.date }
            SortOption.DATE_ASC -> filteredTransactions.sortedBy { it.date }
            SortOption.AMOUNT_DESC -> filteredTransactions.sortedByDescending { it.amount }
            SortOption.AMOUNT_ASC -> filteredTransactions.sortedBy { it.amount }
        }

        _transactions.value = filteredTransactions
        calculateTotals(filteredTransactions)
    }

    /**
     * Add a new transaction (income or expense).
     */
    fun addTransaction(accountId: String, transaction: Transaction) {
        viewModelScope.launch {
            _transactionState.value = TransactionState.Loading
            try {
                val transactionRef = firestore.collection("users")
                    .document(authService.getCurrentUser()?.uid ?: "")
                    .collection("accounts")
                    .document(accountId)
                    .collection("transactions")
                    .document()

                val transactionWithId = transaction.copy(id = transactionRef.id)
                transactionRef.set(transactionWithId).await()

                // Refresh transactions after adding a new one
                loadTransactions(accountId)
                _transactionState.value = TransactionState.Success("Transaction added successfully")
            } catch (e: Exception) {
                _transactionState.value = TransactionState.Error(e.message ?: "Failed to add transaction")
            }
        }
    }

    /**
     * Update an existing transaction.
     */
    fun updateTransaction(transaction: Transaction) {
        val userId = authService.getCurrentUser()?.uid ?: return
        val accountId = transaction.accountId
        _isLoading.value = true
        _error.value = null
        _transactionState.value = TransactionState.Loading

        viewModelScope.launch {
            try {
                firestore.collection("users")
                    .document(userId)
                    .collection("accounts")
                    .document(accountId)
                    .collection("transactions")
                    .document(transaction.id)
                    .set(transaction)
                    .addOnSuccessListener {
                        _transactionState.value = TransactionState.Success()
                        loadTransactions(accountId)
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
        val userId = authService.getCurrentUser()?.uid ?: return
        _isLoading.value = true
        _error.value = null
        _transactionState.value = TransactionState.Loading

        viewModelScope.launch {
            try {
                firestore.collection("users")
                    .document(userId)
                    .collection("accounts")
                    .document(accountId)
                    .collection("transactions")
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

    fun filterByDateRange(startDate: Date, endDate: Date) {
        val fullList = _fullTransactionList
        val filtered = fullList.filter { transaction ->
            val transactionDate = transaction.date.toDate()
            transactionDate in startDate..endDate
        }
        _transactions.value = filtered
        calculateTotals(filtered)
    }

    fun filterByCategory(category: String) {
        val fullList = _fullTransactionList
        val filtered = fullList.filter { it.category == category }
        _transactions.value = filtered
        calculateTotals(filtered)
    }

    fun searchTransactions(query: String) {
        val fullList = _fullTransactionList
        val filtered = fullList.filter { transaction ->
            transaction.description?.contains(query, ignoreCase = true) == true ||
            transaction.category.contains(query, ignoreCase = true)
        }
        _transactions.value = filtered
        calculateTotals(filtered)
    }

    fun getTransaction(transactionId: String): LiveData<Transaction?> {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // First find the transaction in the current list
                val transaction = allTransactions.find { it.id == transactionId }
                if (transaction != null) {
                    _singleTransaction.value = transaction
                    _isLoading.value = false
                    return@launch
                }

                // If not found in current list, try to fetch from Firestore
                val userId = authService.getCurrentUser()?.uid ?: ""
                val accountsRef = firestore.collection("users")
                    .document(userId)
                    .collection("accounts")

                // Get all accounts
                val accounts = accountsRef.get().await().documents

                // Search through each account's transactions
                for (account in accounts) {
                    val transactionDoc = account.reference
                        .collection("transactions")
                        .document(transactionId)
                        .get()
                        .await()

                    if (transactionDoc.exists()) {
                        val transaction = transactionDoc.toObject(Transaction::class.java)
                        _singleTransaction.value = transaction
                        _isLoading.value = false
                        return@launch
                    }
                }

                // If we get here, transaction was not found
                _error.value = "Transaction not found"
                _singleTransaction.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load transaction"
                _singleTransaction.value = null
            } finally {
                _isLoading.value = false
            }
        }
        return singleTransaction
    }

    fun getTransactions(accountId: String): LiveData<List<Transaction>> {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                transactionsCollection
                    .whereEqualTo("accountId", accountId)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            _error.value = e.message
                            _isLoading.value = false
                            return@addSnapshotListener
                        }

                        val transactions = snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject(Transaction::class.java)?.copy(id = doc.id)
                        } ?: emptyList()

                        _transactions.value = transactions
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
        return transactions
    }

    companion object {
        fun formatCurrency(amount: Double): String {
            return CurrencyFormatter.format(amount)
        }

        const val ACCOUNT_ID = "account_id"
        const val CATEGORY = "category"
    }

    fun evaluateAchievements(
        userId: String,
        accounts: List<Account>,
        categories: List<vc.prog3c.poe.data.models.Category>
    ) {
        val evaluator = vc.prog3c.poe.data.services.AchievementEvaluator(
            userId = userId,
            achievementViewModel = achievementViewModel
        )
        evaluator.run(accounts, allTransactions, categories)  // allTransactions already cached
    }


}
