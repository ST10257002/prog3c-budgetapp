package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import vc.prog3c.poe.data.models.Budget
import vc.prog3c.poe.data.models.MonthlyStats
import vc.prog3c.poe.data.repository.BudgetRepository
import vc.prog3c.poe.data.repository.TransactionRepository
import java.util.*

class BudgetViewModel(
    private val budgetRepo: BudgetRepository = BudgetRepository(),
    private val transactionRepo: TransactionRepository = TransactionRepository()
) : ViewModel() {

    private val _budget = MutableLiveData<Budget?>()
    val budget: LiveData<Budget?> = _budget

    private val _monthlyStats = MutableLiveData<MonthlyStats?>()
    val monthlyStats: LiveData<MonthlyStats?> = _monthlyStats

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun refresh() {
        val now = Calendar.getInstance()
        val year = now.get(Calendar.YEAR)
        val month = now.get(Calendar.MONTH) + 1

        viewModelScope.launch {
            // Fetch budget
            val budgetResult = budgetRepo.getBudgetForMonth(year, month)
            budgetResult.onSuccess { _budget.value = it }
                .onFailure { _error.value = it.message ?: "Failed to fetch budget" }

            // Fetch monthly stats
            val statsResult = transactionRepo.getMonthlyStats(year, month)
            statsResult.onSuccess { _monthlyStats.value = it }
                .onFailure { _error.value = it.message ?: "Failed to fetch monthly stats" }
        }
    }

    fun saveBudget(budget: Budget) {
        viewModelScope.launch {
            val result = budgetRepo.saveBudget(budget)
            if (result.isSuccess) {
                refresh()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to save budget"
            }
        }
    }
}
