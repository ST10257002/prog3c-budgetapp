package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> = _categories

    init {
        loadTestData()
    }

    private fun loadTestData() {
        // Test data for expenses
        _expenses.value = listOf(
            Expense(
                id = "1",
                amount = 5000.0,
                category = "Groceries",
                date = Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000),
                description = "Monthly groceries"
            ),
            Expense(
                id = "2",
                amount = 2000.0,
                category = "Transport",
                date = Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000),
                description = "Fuel and maintenance"
            ),
            Expense(
                id = "3",
                amount = 3000.0,
                category = "Entertainment",
                date = Date(System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000),
                description = "Movie night"
            )
        )

        // Test data for categories
        _categories.value = listOf(
            "Groceries",
            "Transport",
            "Entertainment",
            "Bills",
            "Shopping",
            "Health",
            "Education",
            "Other"
        )

        updateTotalExpenses()
    }

    private fun updateTotalExpenses() {
        _totalExpenses.value = _expenses.value?.sumOf { it.amount } ?: 0.0
    }

    fun addExpense(expense: Expense) {
        // TODO: Implement Firestore expense addition
        // - Add to Firestore collection
        // - Handle offline persistence
        // - Implement error handling
        val currentList = _expenses.value?.toMutableList() ?: mutableListOf()
        currentList.add(expense)
        _expenses.value = currentList
        updateTotalExpenses()
    }

    fun updateExpense(expense: Expense) {
        // TODO: Implement Firestore expense update
        // - Update Firestore document
        // - Handle offline persistence
        // - Implement error handling
        val currentList = _expenses.value?.toMutableList() ?: return
        val index = currentList.indexOfFirst { it.id == expense.id }
        if (index != -1) {
            currentList[index] = expense
            _expenses.value = currentList
            updateTotalExpenses()
        }
    }

    fun deleteExpense(expenseId: String) {
        // TODO: Implement Firestore expense deletion
        // - Delete from Firestore collection
        // - Handle offline persistence
        // - Implement error handling
        val currentList = _expenses.value?.toMutableList() ?: return
        currentList.removeIf { it.id == expenseId }
        _expenses.value = currentList
        updateTotalExpenses()
    }

    fun getExpensesForPeriod(startDate: Date, endDate: Date): List<Expense> {
        return _expenses.value?.filter { it.date in startDate..endDate } ?: emptyList()
    }

    fun getExpensesByCategory(category: String): List<Expense> {
        return _expenses.value?.filter { it.category == category } ?: emptyList()
    }
} 