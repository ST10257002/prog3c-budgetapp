package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import vc.prog3c.poe.data.services.FirestoreService
import vc.prog3c.poe.data.repository.SavingsGoalRepository
import java.util.Date
import vc.prog3c.poe.data.models.Card
import vc.prog3c.poe.data.models.Budget
import vc.prog3c.poe.data.models.SavingsGoal
import vc.prog3c.poe.data.models.Category
import vc.prog3c.poe.data.models.IncomeExpenseData
import vc.prog3c.poe.data.models.MonthlyStats
import vc.prog3c.poe.utils.TestData

class DashboardViewModel : ViewModel() {
    // TODO: Replace with Firestore implementation
    // - Create Firestore collections for cards, budgets, savings goals, and categories
    // - Implement real-time listeners for data updates
    // - Add offline persistence support
    // - Implement data synchronization
    // - Add error handling for network issues

    private val _cards = MutableLiveData<List<Card>>()
    val cards: LiveData<List<Card>> = _cards

    private val _currentBudget = MutableLiveData<Budget>()
    val currentBudget: LiveData<Budget> = _currentBudget

    private val _savingsGoals = MutableLiveData<List<SavingsGoal>>()
    val savingsGoals: LiveData<List<SavingsGoal>> = _savingsGoals

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _incomeExpenseData = MutableLiveData<IncomeExpenseData>()
    val incomeExpenseData: LiveData<IncomeExpenseData> = _incomeExpenseData

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
        loadSavingsGoals()
    }

    private fun loadSavingsGoals() {
        SavingsGoalRepository.fetchGoals { goals ->
            _savingsGoals.postValue(goals)
        }
    }

    fun updateSavingsGoal(goalId: String, min: Double, max: Double, budget: Double) {
        val updatedFields = mapOf(
            "minMonthlyGoal" to min,
            "maxMonthlyGoal" to max,
            "monthlyBudget" to budget
        )
        SavingsGoalRepository.updateGoal(goalId, updatedFields) { success ->
            if (success) loadSavingsGoals() // Refresh the list
        }
    }

    fun addSavingsGoal(goal: SavingsGoal) {
        SavingsGoalRepository.saveGoal(goal) { success ->
            if (success) loadSavingsGoals() // Refresh the list
        }
    }

    fun getSavingsProgress(goal: SavingsGoal): Double {
        return if (goal.targetAmount > 0.0) {
            (goal.savedAmount / goal.targetAmount).coerceIn(0.0, 1.0) * 100
        } else {
            0.0
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // TODO: Backend Implementation Required
                // - Fetch dashboard data from Firestore
                // - Handle offline state
                // - Implement error handling
                delay(1000) // Simulate network delay
                loadTestData()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to load dashboard data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshDashboardData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // TODO: Backend Implementation Required
                // - Implement real-time refresh from Firestore
                // - Handle offline state
                // - Implement error handling
                delay(1000) // Simulate network delay
                loadTestData()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to refresh dashboard data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadTestData() {
        _incomeExpenseData.value = TestData.getTestIncomeExpenseData()
        _savingsGoals.value = TestData.getTestSavingsGoals()
        _currentBudget.value = TestData.getTestBudget()
    }

    fun getSavingsProgress(): Double {
        // TODO: Implement Firestore Progress Calculation
        // 1. Calculate progress based on:
        //    - Current savings amount
        //    - Monthly savings goal
        // 2. Update progress in real-time
        // 3. Cache progress calculations
        // 4. Handle edge cases:
        //    - No goal set
        //    - Negative progress
        //    - Goal exceeded
        return 0.0
    }

    /* deprecated
    fun loadGoal() {
        FirestoreService.savingsGoal.fetchGoal { goal ->
            if (goal != null) {
                _savingsGoal.postValue(goal)
            }
        }
    }

    fun updateSavingsGoal(min: Double, max: Double, budget: Double) {
        val goal = vc.prog3c.poe.data.models.SavingsGoal(
            minMonthlyGoal = min,
            maxMonthlyGoal = max,
            monthlyBudget = budget
        )

        FirestoreService.savingsGoal.saveGoal(goal) { success ->
            if (success) {
                _savingsGoal.postValue(goal)
            }
        }
    }
    */

    fun getMonthlyStats(): MonthlyStats {
        // TODO: Implement Firestore Monthly Statistics
        // 1. Query transactions for current month:
        //    - Calculate total income
        //    - Calculate total expenses
        //    - Calculate net savings
        // 2. Group transactions by:
        //    - Category
        //    - Day
        //    - Week
        // 3. Calculate trends:
        //    - Spending patterns
        //    - Income patterns
        //    - Savings rate
        // 4. Cache monthly statistics
        // 5. Update stats in real-time
        return MonthlyStats(0.0, 0.0, 0.0)
    }

    fun getCategoryBreakdown(): Map<String, Double> {
        // TODO: Implement Firestore Category Breakdown
        // 1. Query transactions grouped by category:
        //    - Calculate total per category
        //    - Calculate percentage of total
        // 2. Include both income and expense categories
        // 3. Handle uncategorized transactions
        // 4. Cache category breakdown
        // 5. Update breakdown in real-time
        return emptyMap()
    }

    // TODO: Implement Firestore methods
    fun refreshData() {
        // TODO: Implement Firestore data refresh
        // - Add real-time listeners
        // - Handle offline data
        // - Implement error handling
    }

    fun addNewCard(card: Card) {
        // TODO: Implement Firestore card addition
        // - Add to Firestore collection
        // - Handle offline persistence
        // - Implement error handling
    }

    fun updateBudget(budget: Budget) {
        // TODO: Implement Firestore budget update
        // - Update Firestore document
        // - Handle offline persistence
        // - Implement error handling
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
        // TODO: Implement Firestore current savings update
        // - Update Firestore document
        // - Handle offline persistence
        // - Implement error handling
        _currentSavings.value = amount
    }
}