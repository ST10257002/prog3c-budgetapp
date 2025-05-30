package vc.prog3c.poe.ui.viewmodels

import android.R.attr.data
import androidx.datastore.dataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import vc.prog3c.poe.core.services.AuthService
import vc.prog3c.poe.core.utils.Blogger
import vc.prog3c.poe.data.models.Budget
import vc.prog3c.poe.data.models.Card
import vc.prog3c.poe.data.models.Category
import vc.prog3c.poe.data.models.IncomeExpenseData
import vc.prog3c.poe.data.models.MonthlyStats
import vc.prog3c.poe.data.models.SavingsGoal
import vc.prog3c.poe.data.services.FirestoreService
import vc.prog3c.poe.ui.viewmodels.DashboardUiState.Failure
import vc.prog3c.poe.ui.viewmodels.DashboardUiState.Loading
import vc.prog3c.poe.ui.viewmodels.DashboardUiState.Updated
import java.util.Calendar

class DashboardViewModel(
    private val authService: AuthService = AuthService()
) : ViewModel() {
    companion object {
        private const val TAG = "DashboardViewModel"
    }


    // --- Fields


    private val _uiState = MutableLiveData<DashboardUiState>()
    val uiState: LiveData<DashboardUiState> = _uiState

    
    // TODO: Integrate into a new uiData data class
    private var _statistics: MonthlyStats? = null
    private var _breakdowns: Map<String, Double>? = null


    private val _cards = MutableLiveData<List<Card>>() // TODO: Integrate into uiState
    val cards: LiveData<List<Card>> = _cards // TODO: Integrate into uiState
    

    private val _categories = MutableLiveData<List<Category>>() // TODO: Integrate into uiState
    val categories: LiveData<List<Category>> = _categories // TODO: Integrate into uiState


    private val _incomeExpenseData = MutableLiveData<IncomeExpenseData>() // TODO: Integrate into uiState
    val incomeExpenseData: LiveData<IncomeExpenseData> = _incomeExpenseData // TODO: Integrate into uiState


    private val _currentSavings = MutableLiveData<Double>() // TODO: Integrate into uiState
    val currentSavings: LiveData<Double> = _currentSavings // TODO: Integrate into uiState


    init {
        loadInitialData()
    }


    // --- Internals


    private fun loadInitialData() = viewModelScope.launch {
        try {
            val userId = authService.getCurrentUser()?.uid
            if (userId == null) { // TODO: Is this needed?
                _uiState.value = Failure("User not authenticated")
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
            _uiState.value = Failure("Error loading dashboard: ${e.message}")
        }
    }


    private fun loadSavingsGoals() {
        authService.getCurrentUser()?.uid ?: return // TODO: Is this needed?
        FirestoreService.savingsGoal.fetchGoals { goals ->
            _uiState.value = Updated(savingsGoals = goals)
        }
    }


    private fun loadCurrentBudget(
        year: Int, month: Int
    ) {
        authService.getCurrentUser()?.uid ?: return // TODO: Is this needed?
        FirestoreService.budget.getBudgetForMonth(year, month) { bud ->
            _uiState.value = Updated(budget = bud)
        }
    }


    private fun loadMonthlyStats(
        year: Int, month: Int
    ) {
        authService.getCurrentUser()?.uid ?: return // TODO: Is this needed?
        FirestoreService.transaction.getMonthlyStats(year, month) { stats ->
            _statistics = stats
            _uiState.value = Updated(
                breakdowns = _breakdowns, statistics = _statistics
            )
        }
    }


    // TODO: This needs to be modularised or abstracted into a suspend service -- too large
    private fun loadCategoryBreakdown(
        year: Int, month: Int
    ) {
        val userId = authService.getCurrentUser()?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        Blogger.d(TAG, "Starting category breakdown for user: $userId")

        db.collection("users").document(userId).collection("accounts").get()
            .addOnSuccessListener { accountDocs ->
                val accountIds = accountDocs.map { it.id }
                Blogger.d(TAG, "Fetched accounts: $accountIds")

                val categoryTotals = mutableMapOf<String, Double>()
                val tasks = accountIds.map { accountId ->
                    Blogger.d(TAG, "Fetching transactions for account: $accountId")
                    db.collection("users").document(userId).collection("accounts")
                        .document(accountId).collection("transactions")
                        .whereEqualTo("type", "EXPENSE").get()
                }

                com.google.android.gms.tasks.Tasks.whenAllSuccess<Any>(tasks)
                    .addOnSuccessListener { results ->
                        Blogger.d(TAG, "Fetched ${results.size} transaction result sets")

                        for ((index, res) in results.withIndex()) {
                            val snap = res as com.google.firebase.firestore.QuerySnapshot
                            Blogger.d(
                                TAG,
                                "Account ${accountIds.getOrNull(index)} has ${snap.size()} transactions"
                            )

                            for (doc in snap.documents) {
                                val category = doc.getString("category") ?: "Other"
                                val timestamp = doc.getTimestamp("date")
                                val amount = doc.getDouble("amount") ?: 0.0

                                Blogger.d(
                                    TAG,
                                    "Transaction: category=$category, amount=$amount, timestamp=$timestamp"
                                )

                                if (timestamp != null) {
                                    val cal = Calendar.getInstance()
                                    cal.time = timestamp.toDate()
                                    val docYear = cal.get(Calendar.YEAR)
                                    val docMonth = cal.get(Calendar.MONTH) + 1

                                    // ALL-TIME: Remove the filter
                                    categoryTotals[category] =
                                        (categoryTotals[category] ?: 0.0) + amount
                                }
                            }
                        }

                        Blogger.d(TAG, "Final category totals: $categoryTotals")
                        _breakdowns = categoryTotals
                        _uiState.value = Updated(
                            breakdowns = _breakdowns, statistics = _statistics
                        )
                    }.addOnFailureListener {
                        Blogger.e(TAG, "Failed to fetch transactions: ${it.message}", it)
                        _breakdowns = emptyMap()
                        _uiState.value = Updated(
                            breakdowns = _breakdowns, statistics = _statistics
                        )
                    }
            }.addOnFailureListener {
                Blogger.e(TAG, "Failed to fetch accounts: ${it.message}", it)
                _breakdowns = emptyMap()
                _uiState.value = Updated(
                    breakdowns = _breakdowns, statistics = _statistics
                )
            }
    }


    private fun loadCategories() {
        val userId = authService.getCurrentUser()?.uid ?: return
        FirestoreService.category.getAllCategories { categories ->
            _categories.postValue(categories ?: emptyList())
            if (categories == null) {
                _uiState.value = Failure("Failed to load categories")
            }
        }
    }


    fun updateSavingsGoal(
        goalId: String, min: Double, max: Double, budget: Double, name: String
    ) {
        val updatedFields = mapOf(
            "minMonthlyGoal" to min,
            "maxMonthlyGoal" to max,
            "monthlyBudget" to budget,
            "name" to name
        )
        FirestoreService.savingsGoal.updateGoal(goalId, updatedFields) { success ->
            if (success) loadSavingsGoals()
            else _uiState.value = Failure("Failed to update savings goal")
        }
    }


    fun addSavingsGoal(
        goal: SavingsGoal
    ) {
        FirestoreService.savingsGoal.saveGoal(goal) { success ->
            if (success) loadSavingsGoals()
            else _uiState.value = Failure("Failed to save savings goal")
        }
    }


    fun getSavingsProgress(
        goal: SavingsGoal
    ): Double {
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
        _uiState.value = Loading
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
            PieEntry(totalIncome.toFloat(), "Income"), PieEntry(totalExpenses.toFloat(), "Expenses")
        )

        val dataSet = PieDataSet(entries, "Income vs Expenses")
        dataSet.colors = listOf(
            ColorTemplate.rgb("#4CAF50"), ColorTemplate.rgb("#F44336")
        )

        val pieData = PieData(dataSet)
        pieData.setValueTextSize(12f)
        pieData.setValueTextColor(ColorTemplate.rgb("#FFFFFF"))

        _incomeExpenseData.value = IncomeExpenseData(
            totalIncome = totalIncome, totalExpenses = totalExpenses, pieData = pieData
        )
    }


    fun updateCurrentSavings(amount: Double) {
        // TODO: Persist to Firestore
        _currentSavings.value = amount
    }
}
