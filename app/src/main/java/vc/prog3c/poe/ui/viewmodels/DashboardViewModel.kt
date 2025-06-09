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
import java.util.Date
import vc.prog3c.poe.core.services.AchievementEngine
import vc.prog3c.poe.data.models.Account
import vc.prog3c.poe.data.models.Transaction
import java.util.UUID


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
        loadMonthlyStats()
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

    private fun loadMonthlyStats() {
        val userId = authService.getCurrentUser()?.uid ?: return
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        // Get all accounts and calculate total expenses
        FirestoreService.account.getAllAccounts { accounts ->
            var totalExpenses = 0.0
            var processedAccounts = 0

            accounts.forEach { account ->
                FirestoreService.account.getTransactionsForAccount(account.id) { transactions ->
                    // Filter transactions for current month and sum expenses
                    totalExpenses += transactions
                        .filter { 
                            val txDate = it.date.toDate()
                            txDate.month == currentMonth && 
                            txDate.year + 1900 == currentYear &&
                            it.type == TransactionType.EXPENSE
                        }
                        .sumOf { it.amount }

                    processedAccounts++

                    // When all accounts are processed
                    if (processedAccounts == accounts.size) {
                        // Get the budget from the first savings goal (since it contains the monthly budget)
                        FirestoreService.savingsGoal.fetchGoals { goals ->
                            val goal = goals.firstOrNull()
                            val monthlyBudget = goal?.monthlyBudget ?: 0.0
                            
                            val stats = MonthlyStats(
                                totalExpenses = totalExpenses,
                                budget = monthlyBudget
                            )
                            _statistics = stats
                            currentBudget = Budget(
                                max = monthlyBudget,
                                min = goal?.minMonthlyGoal ?: 0.0,
                                month = currentMonth + 1,
                                year = currentYear
                            )
                            emitUpdatedState()
                        }
                    }
                }
            }
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

    fun updateBudget(budget: Budget) {
        FirestoreService.budget.updateBudget(budget) { success ->
            if (success) {
                currentBudget = budget
                emitUpdatedState()
            } else {
                _uiState.value = Failure("Failed to update budget")
            }
        }
    }

    fun updateSavingsGoal(goal: SavingsGoal) {
        val updates = mapOf<String, Any>(
            "name" to goal.name,
            "targetAmount" to goal.targetAmount,
            "savedAmount" to goal.savedAmount,
            "targetDate" to (goal.targetDate ?: Date()),
            "minMonthlyGoal" to goal.minMonthlyGoal,
            "maxMonthlyGoal" to goal.maxMonthlyGoal,
            "monthlyBudget" to goal.monthlyBudget
        )
        
        FirestoreService.savingsGoal.updateGoal(goal.id, updates) { success ->
            if (success) {
                currentSavingsGoals = currentSavingsGoals?.map {
                    if (it.id == goal.id) goal else it
                }
                emitUpdatedState()
            } else {
                _uiState.value = Failure("Failed to update savings goal")
            }
        }
    }

    fun contributeToSavingsGoal(goalId: String, amount: Double) {
        viewModelScope.launch {
            try {
                // Find the goal in our current list
                val goal = currentSavingsGoals?.find { it.id == goalId }
                if (goal == null) {
                    _uiState.value = DashboardUiState.Failure("Savings goal not found")
                    return@launch
                }

                // Get all accounts to calculate total income
                FirestoreService.account.getAllAccounts { accounts ->
                    var totalIncome = 0.0
                    var processedAccounts = 0

                    // Calculate total income from all accounts
                    accounts.forEach { account ->
                        FirestoreService.account.getTransactionsForAccount(account.id) { transactions ->
                            totalIncome += transactions
                                .filter { it.type == TransactionType.INCOME }
                                .sumOf { it.amount }
                            
                            processedAccounts++
                            
                            // When all accounts are processed, check if contribution is valid
                            if (processedAccounts == accounts.size) {
                                if (amount > totalIncome) {
                                    _uiState.value = DashboardUiState.Failure("Contribution amount exceeds available income")
                                    return@getTransactionsForAccount
                                }

                                // Create a new income transaction to record the contribution
                                val contributionTransaction = Transaction(
                                    id = UUID.randomUUID().toString(),
                                    type = TransactionType.EXPENSE, // Mark as expense to subtract from income
                                    amount = amount,
                                    description = "Contribution to ${goal.name}",
                                    date = com.google.firebase.Timestamp.now(),
                                    category = "Savings",
                                    accountId = accounts.first().id, // Use first account for now
                                    userId = authService.getCurrentUser()?.uid ?: return@getTransactionsForAccount
                                )

                                // Add the transaction first
                                FirestoreService.transaction.addTransaction(contributionTransaction) { success ->
                                    if (!success) {
                                        _uiState.value = DashboardUiState.Failure("Failed to record contribution")
                                        return@addTransaction
                                    }

                                    // Then update the goal
                                    val updatedGoal = goal.copy(
                                        savedAmount = goal.savedAmount + amount,
                                        lastContributionDate = Date()
                                    )

                                    val updates = mapOf<String, Any>(
                                        "savedAmount" to updatedGoal.savedAmount,
                                        "lastContributionDate" to updatedGoal.lastContributionDate!!
                                    )
                                    
                                    FirestoreService.savingsGoal.updateGoal(goalId, updates) { success ->
                                        if (success) {
                                            // Update local state
                                            currentSavingsGoals = currentSavingsGoals?.map { 
                                                if (it.id == goalId) updatedGoal else it 
                                            }
                                            emitUpdatedState()
                                        } else {
                                            _uiState.value = DashboardUiState.Failure("Failed to update savings goal")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Failure("Failed to contribute to savings goal: ${e.message}")
            }
        }
    }

    fun getMonthlyBudgetForGoal(goalId: String): Double {
        return currentSavingsGoals?.find { it.id == goalId }?.monthlyBudget ?: 0.0
    }

    fun getMonthlyContributionForGoal(goalId: String): Double {
        return currentSavingsGoals?.find { it.id == goalId }?.monthlyContribution ?: 0.0
    }

    fun updateCategory(category: Category) {
        val userId = authService.getCurrentUser()?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        
        db.collection("users").document(userId).collection("categories")
            .document(category.id)
            .set(category)
            .addOnSuccessListener {
                currentCategoryList = currentCategoryList?.map { 
                    if (it.id == category.id) category else it 
                }
                emitUpdatedState()
            }
            .addOnFailureListener {
                _uiState.value = Failure("Failed to update category")
            }
    }

    fun deleteCategory(categoryId: String) {
        val userId = authService.getCurrentUser()?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        
        db.collection("users").document(userId).collection("categories")
            .document(categoryId)
            .delete()
            .addOnSuccessListener {
                currentCategoryList = currentCategoryList?.filter { it.id != categoryId }
                emitUpdatedState()
            }
            .addOnFailureListener {
                _uiState.value = Failure("Failed to delete category")
            }
    }
}