// DashboardViewModel.kt
package vc.prog3c.poe.ui.viewmodels

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
import vc.prog3c.poe.data.models.*
import vc.prog3c.poe.data.services.FirestoreService
import vc.prog3c.poe.ui.viewmodels.DashboardUiState.*
import java.util.Calendar
import vc.prog3c.poe.core.services.AchievementEngine
import vc.prog3c.poe.data.models.Account
import vc.prog3c.poe.data.models.Transaction


class DashboardViewModel(
    private val authService: AuthService = AuthService()
) : ViewModel() {
    companion object {
        private const val TAG = "DashboardViewModel"
    }

    private val _uiState = MutableLiveData<DashboardUiState>()
    val uiState: LiveData<DashboardUiState> = _uiState

    private var _statistics: MonthlyStats? = null
    private var _breakdowns: Map<String, Double>? = null

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private var currentSavingsGoals: List<SavingsGoal>? = null
    private var currentBudget: Budget? = null
    private var currentCategoryList: List<Category>? = null

    init {
        loadInitialData()
    }

    fun refreshData() {
        _uiState.value = Loading
        loadInitialData()
    }

    private fun emitUpdatedState() {
        _uiState.value = Updated(
            categoryList = currentCategoryList,
            breakdowns = _breakdowns,
            statistics = _statistics,
            savingsGoals = currentSavingsGoals,
            budget = currentBudget
        )
    }

    private fun loadInitialData() = viewModelScope.launch {
        val userId = authService.getCurrentUser()?.uid ?: run {
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
    }

    private fun loadSavingsGoals() {
        FirestoreService.savingsGoal.fetchGoals { goals ->
            currentSavingsGoals = goals
            emitUpdatedState()
        }
    }

    private fun loadCurrentBudget(year: Int, month: Int) {
        FirestoreService.budget.getBudgetForMonth(year, month) { bud ->
            currentBudget = bud
            emitUpdatedState()
        }
    }

    private fun loadMonthlyStats(year: Int, month: Int) {
        FirestoreService.budget.getMonthlyStats(year, month) { stats ->
            _statistics = stats
            emitUpdatedState()
        }
    }

    private fun loadCategoryBreakdown(year: Int, month: Int) {
        val userId = authService.getCurrentUser()?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).collection("accounts").get()
            .addOnSuccessListener { accountDocs ->
                val accountIds = accountDocs.map { it.id }
                val categoryTotals = mutableMapOf<String, Double>()
                val tasks = accountIds.map { accountId ->
                    db.collection("users").document(userId).collection("accounts")
                        .document(accountId).collection("transactions")
                        .whereEqualTo("type", "EXPENSE").get()
                }

                com.google.android.gms.tasks.Tasks.whenAllSuccess<Any>(tasks)
                    .addOnSuccessListener { results ->
                        for ((index, res) in results.withIndex()) {
                            val snap = res as com.google.firebase.firestore.QuerySnapshot
                            for (doc in snap.documents) {
                                val category = doc.getString("category") ?: "Other"
                                val amount = doc.getDouble("amount") ?: 0.0
                                categoryTotals[category] =
                                    (categoryTotals[category] ?: 0.0) + amount
                            }
                        }
                        _breakdowns = categoryTotals
                        emitUpdatedState()
                    }
                    .addOnFailureListener {
                        _breakdowns = emptyMap()
                        emitUpdatedState()
                    }
            }
            .addOnFailureListener {
                _breakdowns = emptyMap()
                emitUpdatedState()
            }
    }

    private fun loadCategories() {
        val userId = authService.getCurrentUser()?.uid ?: return
        FirestoreService.category.getAllCategories { categories ->
            val finalCategories = categories ?: emptyList()
            _categories.postValue(finalCategories)
            currentCategoryList = finalCategories
            emitUpdatedState()
        }
    }



}