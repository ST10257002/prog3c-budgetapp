package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

data class Expense(
    val id: String,
    val amount: Double,
    val category: String,
    val date: Date,
    val description: String? = null
)

class ExpenseViewModel : ViewModel() {
    // TODO: Replace with Firestore implementation
    // - Create Firestore collection for expenses
    // - Implement real-time listeners for expense updates
    // - Add offline persistence support
    // - Implement data synchronization
    // - Add error handling for network issues

    private val _expenses = MutableLiveData<List<Expense>>()
    val expenses: LiveData<List<Expense>> = _expenses

    private val _totalExpenses = MutableLiveData<Double>()
    val totalExpenses: LiveData<Double> = _totalExpenses

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> = _categories

    private var lastOperation: (() -> Unit)? = null

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // TODO: Backend Implementation Required
                // - Fetch expenses from Firestore
                // - Handle offline state
                // - Implement error handling
                delay(1000) // Simulate network delay
                loadTestData()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to load expenses: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadTestData() {
        // Test data for expenses
        _expenses.value = listOf(
            Expense(
                id = "1",
                amount = 100.0,
                category = "Food",
                date = Date(),
                description = "Groceries"
            ),
            Expense(
                id = "2",
                amount = 50.0,
                category = "Transport",
                date = Date(),
                description = "Bus fare"
            )
        )

        // Test data for categories
        _categories.value = listOf(
            "Food",
            "Transport",
            "Housing",
            "Utilities",
            "Entertainment",
            "Shopping",
            "Healthcare",
            "Education",
            "Travel",
            "Other"
        )

        updateTotalExpenses()
    }

    private fun updateTotalExpenses() {
        _totalExpenses.value = _expenses.value?.sumOf { it.amount } ?: 0.0
    }

    fun addExpense(expense: Expense) {
        lastOperation = { addExpense(expense) }
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // TODO: Backend Implementation Required
                // - Add to Firestore collection
                // - Handle offline persistence
                // - Implement error handling
                delay(1000) // Simulate network delay
                val currentList = _expenses.value?.toMutableList() ?: mutableListOf()
                currentList.add(expense)
                _expenses.value = currentList
                updateTotalExpenses()
                _saveSuccess.value = true
                _error.value = null
            } catch (e: Exception) {
                _error.value = when (e) {
                    is IllegalArgumentException -> "Invalid expense data: ${e.message}"
                    is IllegalStateException -> "Failed to save expense: ${e.message}"
                    else -> "An unexpected error occurred: ${e.message}"
                }
                _saveSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun retryLastOperation() {
        lastOperation?.invoke()
    }

    fun updateExpense(expense: Expense) {
        lastOperation = { updateExpense(expense) }
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // TODO: Backend Implementation Required
                // - Update Firestore document
                // - Handle offline persistence
                // - Implement error handling
                delay(1000) // Simulate network delay
                val currentList = _expenses.value?.toMutableList() ?: return@launch
                val index = currentList.indexOfFirst { it.id == expense.id }
                if (index != -1) {
                    currentList[index] = expense
                    _expenses.value = currentList
                    updateTotalExpenses()
                    _saveSuccess.value = true
                    _error.value = null
                }
            } catch (e: Exception) {
                _error.value = when (e) {
                    is IllegalArgumentException -> "Invalid expense data: ${e.message}"
                    is IllegalStateException -> "Failed to update expense: ${e.message}"
                    else -> "An unexpected error occurred: ${e.message}"
                }
                _saveSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteExpense(expenseId: String) {
        lastOperation = { deleteExpense(expenseId) }
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // TODO: Backend Implementation Required
                // - Delete from Firestore collection
                // - Handle offline persistence
                // - Implement error handling
                delay(1000) // Simulate network delay
                val currentList = _expenses.value?.toMutableList() ?: return@launch
                currentList.removeIf { it.id == expenseId }
                _expenses.value = currentList
                updateTotalExpenses()
                _saveSuccess.value = true
                _error.value = null
            } catch (e: Exception) {
                _error.value = when (e) {
                    is IllegalArgumentException -> "Invalid expense ID: ${e.message}"
                    is IllegalStateException -> "Failed to delete expense: ${e.message}"
                    else -> "An unexpected error occurred: ${e.message}"
                }
                _saveSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getExpensesForPeriod(startDate: Date, endDate: Date): List<Expense> {
        return _expenses.value?.filter { it.date in startDate..endDate } ?: emptyList()
    }

    fun getExpensesByCategory(category: String): List<Expense> {
        return _expenses.value?.filter { it.category == category } ?: emptyList()
    }

    fun getTotalExpensesByCategory(category: String): Double {
        return getExpensesByCategory(category).sumOf { it.amount }
    }
} 