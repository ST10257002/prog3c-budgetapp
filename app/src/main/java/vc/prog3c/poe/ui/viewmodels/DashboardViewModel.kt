package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import vc.prog3c.poe.data.services.FirestoreService
import java.util.Date
import vc.prog3c.poe.data.models.Card
import vc.prog3c.poe.data.models.Budget
import vc.prog3c.poe.data.models.SavingsGoal
import vc.prog3c.poe.data.models.Category
import vc.prog3c.poe.data.models.IncomeExpenseData
import vc.prog3c.poe.data.models.MonthlyStats

class DashboardViewModel : ViewModel() {

    private val _cards = MutableLiveData<List<Card>>()
    val cards: LiveData<List<Card>> = _cards

//    private val _currentBudget = MutableLiveData<Budget>()
//    val currentBudget: LiveData<Budget> = _currentBudget

    private val _savingsGoals = MutableLiveData<List<SavingsGoal>>()
    val savingsGoals: LiveData<List<SavingsGoal>> = _savingsGoals

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _incomeExpenseData = MutableLiveData<IncomeExpenseData>()
    val incomeExpenseData: LiveData<IncomeExpenseData> = _incomeExpenseData

    private val _currentSavings = MutableLiveData<Double>()
    val currentSavings: LiveData<Double> = _currentSavings

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _budget = MutableLiveData<Budget?>()
    val budget: LiveData<Budget?> = _budget

    private val _monthlyStats = MutableLiveData<MonthlyStats?>()
    val monthlyStats: LiveData<MonthlyStats?> = _monthlyStats

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                loadSavingsGoals()

                val cal = java.util.Calendar.getInstance()
                val year = cal.get(java.util.Calendar.YEAR)
                val month = cal.get(java.util.Calendar.MONTH) + 1

                loadCurrentBudget(year, month)
                loadMonthlyStats(year, month)

                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to load dashboard data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }



    private fun loadSavingsGoals() {
        FirestoreService.savingsGoal.fetchGoals { goals ->
            if (goals != null) {
                _savingsGoals.postValue(goals)
            } else {
                _error.postValue("Failed to load savings goals")
            }
        }
    }

    private fun loadCurrentBudget(year: Int, month: Int) {
        FirestoreService.budget.getBudgetForMonth(year, month) { bud ->
            _budget.postValue(bud)
        }
    }

    private fun loadMonthlyStats(year: Int, month: Int) {
        FirestoreService.transaction.getMonthlyStats(year, month) { stats ->
            _monthlyStats.postValue(stats)
        }
    }

    fun updateSavingsGoal(goalId: String, min: Double, max: Double, budget: Double, name: String) {
        val updatedFields = mapOf(
            "minMonthlyGoal" to min,
            "maxMonthlyGoal" to max,
            "monthlyBudget" to budget,
            "name" to name
        )
        FirestoreService.savingsGoal.updateGoal(goalId, updatedFields) { success ->
            if (success) loadSavingsGoals()
            else _error.postValue("Failed to update savings goal")
        }
    }


    fun addSavingsGoal(goal: SavingsGoal) {
        FirestoreService.savingsGoal.saveGoal(goal) { success ->
            if (success) loadSavingsGoals()
            else _error.postValue("Failed to save savings goal")
        }
    }

    fun getSavingsProgress(goal: SavingsGoal): Double {
        return if (goal.targetAmount > 0.0) {
            (goal.savedAmount / goal.targetAmount).coerceIn(0.0, 1.0) * 100
        } else {
            0.0
        }
    }

    fun getMonthlyStats(): MonthlyStats {
        // TODO: Implement Firestore Monthly Statistics
        return MonthlyStats(0.0, 0.0, 0.0)
    }

    fun getCategoryBreakdown(): Map<String, Double> {
        // TODO: Implement Firestore Category Breakdown
        return emptyMap()
    }

    fun refreshData() {
        loadInitialData()
    }

    fun addNewCard(card: Card) {
        // TODO: Implement Firestore card addition
    }

    fun updateBudget(budget: Budget) {
        // TODO: Implement Firestore budget update
    }

    fun updateIncomeExpenseData(totalIncome: Double, totalExpenses: Double) {
        val entries = listOf(
            PieEntry(totalIncome.toFloat(), "Income"),
            PieEntry(totalExpenses.toFloat(), "Expenses")
        )

        val dataSet = PieDataSet(entries, "Income vs Expenses")
        dataSet.colors = listOf(
            ColorTemplate.rgb("#4CAF50"),
            ColorTemplate.rgb("#F44336")
        )

        val pieData = PieData(dataSet)
        pieData.setValueTextSize(12f)
        pieData.setValueTextColor(ColorTemplate.rgb("#FFFFFF"))

        _incomeExpenseData.value = IncomeExpenseData(
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            pieData = pieData
        )
    }

    fun updateCurrentSavings(amount: Double) {
        // TODO: Persist to Firestore
        _currentSavings.value = amount
    }
}
