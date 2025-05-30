package vc.prog3c.poe.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.firestore.FirebaseFirestore
import vc.prog3c.poe.core.services.AuthService
import vc.prog3c.poe.data.services.FirestoreService
import vc.prog3c.poe.data.models.Card
import vc.prog3c.poe.data.models.Budget
import vc.prog3c.poe.data.models.SavingsGoal
import vc.prog3c.poe.data.models.IncomeExpenseData
import vc.prog3c.poe.data.models.MonthlyStats
import java.util.Calendar
import vc.prog3c.poe.data.models.Category

class DashboardViewModel(
    private val authService: AuthService = AuthService()
) : ViewModel() {

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

    private val _categoryBreakdown = MutableLiveData<Map<String, Double>>()
    val categoryBreakdown: LiveData<Map<String, Double>> = _categoryBreakdown

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                val userId = authService.getCurrentUser()?.uid
                if (userId == null) {
                    _error.postValue("User not authenticated")
                    return@launch
                }

                val cal = Calendar.getInstance()
                val year = cal.get(Calendar.YEAR)
                val month = cal.get(Calendar.MONTH) + 1

                loadSavingsGoals()
                loadCurrentBudget(year, month)
                loadMonthlyStats(year, month)
                loadCategoryBreakdown(year, month)
                loadCategories()
            } catch (e: Exception) {
                _error.postValue("Error loading dashboard: ${e.message}")
            }
        }
    }

    private fun loadSavingsGoals() {
        val userId = authService.getCurrentUser()?.uid ?: return
        FirestoreService.savingsGoal.fetchGoals { goals ->
            if (goals != null) {
                _savingsGoals.postValue(goals)
            } else {
                _error.postValue("Failed to load savings goals")
            }
        }
    }

    private fun loadCurrentBudget(year: Int, month: Int) {
        val userId = authService.getCurrentUser()?.uid ?: return
        FirestoreService.budget.getBudgetForMonth(year, month) { bud ->
            _budget.postValue(bud)
        }
    }

    private fun loadMonthlyStats(year: Int, month: Int) {
        val userId = authService.getCurrentUser()?.uid ?: return
        FirestoreService.transaction.getMonthlyStats(year, month) { stats ->
            _monthlyStats.postValue(stats)
        }
    }

    private fun loadCategoryBreakdown(year: Int, month: Int) {
        val userId = authService.getCurrentUser()?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        Log.d("DASH_TEST", "Starting category breakdown for user: $userId")

        db.collection("users").document(userId).collection("accounts")
            .get()
            .addOnSuccessListener { accountDocs ->
                val accountIds = accountDocs.map { it.id }
                Log.d("DASH_TEST", "Fetched accounts: $accountIds")

                val categoryTotals = mutableMapOf<String, Double>()
                val tasks = accountIds.map { accountId ->
                    Log.d("DASH_TEST", "Fetching transactions for account: $accountId")
                    db.collection("users").document(userId)
                        .collection("accounts").document(accountId)
                        .collection("transactions")
                        .whereEqualTo("type", "EXPENSE")
                        .get()
                }

                com.google.android.gms.tasks.Tasks.whenAllSuccess<Any>(tasks)
                    .addOnSuccessListener { results ->
                        Log.d("DASH_TEST", "Fetched ${results.size} transaction result sets")

                        for ((index, res) in results.withIndex()) {
                            val snap = res as com.google.firebase.firestore.QuerySnapshot
                            Log.d("DASH_TEST", "Account ${accountIds.getOrNull(index)} has ${snap.size()} transactions")

                            for (doc in snap.documents) {
                                val category = doc.getString("category") ?: "Other"
                                val timestamp = doc.getTimestamp("date")
                                val amount = doc.getDouble("amount") ?: 0.0

                                Log.d("DASH_TEST", "Transaction: category=$category, amount=$amount, timestamp=$timestamp")

                                if (timestamp != null) {
                                    val cal = Calendar.getInstance()
                                    cal.time = timestamp.toDate()
                                    val docYear = cal.get(Calendar.YEAR)
                                    val docMonth = cal.get(Calendar.MONTH) + 1

                                    // ALL-TIME: Remove the filter
                                    categoryTotals[category] = (categoryTotals[category] ?: 0.0) + amount
                                }
                            }
                        }

                        Log.d("DASH_TEST", "Final category totals: $categoryTotals")
                        _categoryBreakdown.postValue(categoryTotals)
                    }
                    .addOnFailureListener {
                        Log.e("DASH_TEST", "Failed to fetch transactions: ${it.message}", it)
                        _categoryBreakdown.postValue(emptyMap())
                    }
            }
            .addOnFailureListener {
                Log.e("DASH_TEST", "Failed to fetch accounts: ${it.message}", it)
                _categoryBreakdown.postValue(emptyMap())
            }
    }

    private fun loadCategories() {
        val userId = authService.getCurrentUser()?.uid ?: return
        FirestoreService.category.getAllCategories { categories ->
            _categories.postValue(categories ?: emptyList())
            if (categories == null) {
                _error.postValue("Failed to load categories")
            }
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
