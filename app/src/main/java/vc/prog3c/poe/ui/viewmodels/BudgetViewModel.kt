package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vc.prog3c.poe.data.models.Budget
import vc.prog3c.poe.data.models.MonthlyStats
import vc.prog3c.poe.data.repository.BudgetRepository
import vc.prog3c.poe.data.repository.TransactionRepository // you must already have this
import java.util.Calendar

class BudgetViewModel : ViewModel() {
    private val budgetRepo = BudgetRepository()
    private val transactionRepo = TransactionRepository()

    private val _budget = MutableLiveData<Budget?>()
    val budget: LiveData<Budget?> = _budget

    private val _monthlyStats = MutableLiveData<MonthlyStats?>()
    val monthlyStats: LiveData<MonthlyStats?> = _monthlyStats

    fun loadBudgetAndStats() {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based

        // Load budget
        budgetRepo.getBudgetForMonth(year, month) { bud ->
            _budget.postValue(bud)
        }

        // Load stats (expenses + income)
        transactionRepo.getMonthlyStats(year, month) { stats ->
            _monthlyStats.postValue(stats)
        }
    }

    fun saveBudget(budget: Budget) {
        budgetRepo.saveBudget(budget) { success ->
            if (success) loadBudgetAndStats()
            // You can add error handling here
        }
    }

    // This is in BudgetViewModel
    fun refreshDashboardData() {
        // Get this month/year
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1

        budgetRepo.getBudgetForMonth(year, month) { bud ->
            _budget.postValue(bud)
        }

        transactionRepo.getMonthlyStats(year, month) { stats ->
            _monthlyStats.postValue(stats)
        }
    }

}
