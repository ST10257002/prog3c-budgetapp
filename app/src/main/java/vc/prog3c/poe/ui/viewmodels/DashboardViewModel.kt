package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import vc.prog3c.poe.data.services.FirestoreService
import java.util.Date
import vc.prog3c.poe.data.models.*
import java.util.Calendar

class DashboardViewModel : ViewModel() {

    private val _cards = MutableLiveData<List<Card>>()
    val cards: LiveData<List<Card>> = _cards

    private val _currentBudget = MutableLiveData<Budget>()
    val currentBudget: LiveData<Budget> = _currentBudget

    private val _savingsGoals = MutableLiveData<List<SavingsGoal>>()
    val savingsGoals: LiveData<List<SavingsGoal>> = _savingsGoals

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _currentSavings = MutableLiveData<Double>()
    val currentSavings: LiveData<Double> = _currentSavings

    private val _monthlyStats = MutableLiveData<MonthlyStats>()
    val monthlyStats: LiveData<MonthlyStats> = _monthlyStats

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                loadMockData()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to load dashboard data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadMockData() {
        // Mock Savings Goals
        val mockSavingsGoal = SavingsGoal(
            id = "1",
            name = "New Car",
            targetAmount = 250000.0,
            savedAmount = 75000.0,
            targetDate = Calendar.getInstance().apply { add(Calendar.MONTH, 12) }.time,
            minMonthlyGoal = 5000.0,
            maxMonthlyGoal = 10000.0,
            monthlyBudget = 7500.0
        )
        _savingsGoals.value = listOf(mockSavingsGoal)

        // Mock Categories with correct TransactionType
        val mockCategories = listOf(
            Category("1", "Groceries", TransactionType.EXPENSE, "ic_food", "#FF5722"),
            Category("2", "Transport", TransactionType.EXPENSE, "ic_car", "#2196F3"),
            Category("3", "Entertainment", TransactionType.EXPENSE, "ic_movie", "#9C27B0"),
            Category("4", "Salary", TransactionType.INCOME, "ic_money", "#4CAF50"),
            Category("5", "Bills", TransactionType.EXPENSE, "ic_bill", "#F44336")
        )
        _categories.value = mockCategories

        // Mock Monthly Stats
        val mockMonthlyStats = MonthlyStats(
            totalIncome = 25000.0,
            totalExpenses = 15000.0,
            savings = 10000.0
        )
        _monthlyStats.value = mockMonthlyStats

        // Mock Current Budget
        val mockBudget = Budget(
            id = "1",
            month = "March 2024",
            amount = 20000.0,
            spent = 15000.0
        )
        _currentBudget.value = mockBudget

        // Mock Current Savings
        _currentSavings.value = 75000.0
    }

    fun getSavingsProgress(goal: SavingsGoal): Double {
        return if (goal.targetAmount > 0.0) {
            (goal.savedAmount / goal.targetAmount).coerceIn(0.0, 1.0) * 100
        } else {
            0.0
        }
    }

    fun getMonthlyStats(): MonthlyStats {
        return _monthlyStats.value ?: MonthlyStats(0.0, 0.0, 0.0)
    }

    fun getCategoryBreakdown(): Map<String, Double> {
        // Mock category breakdown data
        return mapOf(
            "Groceries" to 3000.0,
            "Transport" to 2000.0,
            "Entertainment" to 1500.0,
            "Bills" to 8500.0
        )
    }

    fun refreshData() {
        loadInitialData()
    }

    // Mock function to update savings goal parameters
    fun updateSavingsGoal(goalId: String, minMonthlyGoal: Double, maxMonthlyGoal: Double, monthlyBudget: Double) {
        // This is a mock implementation. In a real app, you would update your data source (e.g., Firestore).
        // For now, we'll just log the update or update the local mock data structure if needed.
        println("Mock updateSavingsGoal called for goalId: $goalId with min: $minMonthlyGoal, max: $maxMonthlyGoal, budget: $monthlyBudget")
        
        // Optional: Update local mock data to reflect the change in UI immediately
        val currentGoals = _savingsGoals.value?.toMutableList() ?: mutableListOf()
        val index = currentGoals.indexOfFirst { it.id == goalId }
        if (index != -1) {
            val updatedGoal = currentGoals[index].copy(
                minMonthlyGoal = minMonthlyGoal,
                maxMonthlyGoal = maxMonthlyGoal,
                monthlyBudget = monthlyBudget
            )
            currentGoals[index] = updatedGoal
            _savingsGoals.value = currentGoals
        }
    }
}
