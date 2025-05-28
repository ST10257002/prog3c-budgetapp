package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import vc.prog3c.poe.data.models.*
import vc.prog3c.poe.data.repository.*
import java.util.*

class DashboardViewModel(
    private val savingsGoalRepo: SavingsGoalRepository = SavingsGoalRepository(),
    private val budgetRepo: BudgetRepository = BudgetRepository(),
    private val transactionRepo: TransactionRepository = TransactionRepository()
) : ViewModel() {

    private val _savingsGoals = MutableLiveData<List<SavingsGoal>>()
    val savingsGoals: LiveData<List<SavingsGoal>> = _savingsGoals

    private val _budget = MutableLiveData<Budget?>()
    val budget: LiveData<Budget?> = _budget

    private val _monthlyStats = MutableLiveData<MonthlyStats>()
    val monthlyStats: LiveData<MonthlyStats> = _monthlyStats

    private val _categoryBreakdown = MutableLiveData<Map<String, Double>>()
    val categoryBreakdown: LiveData<Map<String, Double>> = _categoryBreakdown

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        refreshData()
    }

    fun refreshData() {
        val now = Calendar.getInstance()
        val year = now.get(Calendar.YEAR)
        val month = now.get(Calendar.MONTH) + 1

        loadSavingsGoals()
        loadBudget(year, month)
        loadStats(year, month)
        loadCategoryTotals()
    }

    private fun loadSavingsGoals() {
        viewModelScope.launch {
            val result = savingsGoalRepo.fetchGoals()
            result.onSuccess { _savingsGoals.value = it }
                .onFailure { _error.value = it.message ?: "Failed to fetch savings goals" }
        }
    }

    private fun loadBudget(year: Int, month: Int) {
        viewModelScope.launch {
            val result = budgetRepo.getBudgetForMonth(year, month)
            result.onSuccess { _budget.value = it }
                .onFailure { _error.value = it.message ?: "Failed to fetch budget" }
        }
    }

    private fun loadStats(year: Int, month: Int) {
        viewModelScope.launch {
            val result = transactionRepo.getMonthlyStats(year, month)
            result.onSuccess { _monthlyStats.value = it }
                .onFailure { _error.value = it.message ?: "Failed to fetch stats" }
        }
    }

    private fun loadCategoryTotals() {
        viewModelScope.launch {
            val result = transactionRepo.getCategoryTotals()
            result.onSuccess { _categoryBreakdown.value = it }
                .onFailure { _error.value = it.message ?: "Failed to fetch categories" }
        }
    }

    fun addSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            val result = savingsGoalRepo.saveGoal(goal)
            if (result.isSuccess) loadSavingsGoals()
            else _error.value = result.exceptionOrNull()?.message ?: "Failed to add goal"
        }
    }

    fun updateSavingsGoal(goalId: String, min: Double, max: Double, budget: Double, name: String) {
        val updates = mapOf(
            "minMonthlyGoal" to min,
            "maxMonthlyGoal" to max,
            "monthlyBudget" to budget,
            "name" to name
        )

        viewModelScope.launch {
            savingsGoalRepo.updateGoal(goalId, updates)
            loadSavingsGoals()
        }
    }


    fun getSavingsProgress(goal: SavingsGoal): Double {
        return if (goal.targetAmount > 0.0)
            (goal.savedAmount / goal.targetAmount).coerceIn(0.0, 1.0) * 100
        else 0.0
    }
}
