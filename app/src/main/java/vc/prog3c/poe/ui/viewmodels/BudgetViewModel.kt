package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vc.prog3c.poe.data.models.Budget
import vc.prog3c.poe.data.models.MonthlyStats
import vc.prog3c.poe.data.services.FirestoreService
import java.util.Calendar
/**
 * @reference Firebase Firestore - Document Reads and Updates: https://firebase.google.com/docs/firestore/manage-data/add-data#update-data
 * @reference Android LiveData - Lifecycle-Aware Observables: https://developer.android.com/topic/libraries/architecture/livedata
 */

class BudgetViewModel : ViewModel() {
    companion object {
        private const val TAG = "BudgetViewModel"
    }
    
    
    // --- Fields
    
    
    private val _budget = MutableLiveData<Budget?>()
    val budget: LiveData<Budget?> = _budget

    
    private val _monthlyStats = MutableLiveData<MonthlyStats?>()
    val monthlyStats: LiveData<MonthlyStats?> = _monthlyStats

    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    
    // --- Internals
    
    
    fun loadBudgetAndStats() {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based

        // Load budget
        FirestoreService.budget.getBudgetForMonth(year, month) { bud ->
            _budget.postValue(bud)
        }

        // Load stats (expenses + income)
        FirestoreService.budget.getMonthlyStats(year, month) { stats ->
            _monthlyStats.postValue(stats)
        }
    }


    // This is in BudgetViewModel
    fun refreshDashboardData() {
        // Get this month/year
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1

        FirestoreService.budget.getBudgetForMonth(year, month) { bud ->
            _budget.postValue(bud)
        }

        FirestoreService.budget.getMonthlyStats(year, month) { stats ->
            _monthlyStats.postValue(stats)
        }
    }

    fun updateBudget(budget: Budget) {
        FirestoreService.budget.updateBudget(budget) { success ->
            if (success) {
                _budget.postValue(budget)
                _error.postValue(null)
            } else {
                _error.postValue("Failed to update budget")
            }
        }
    }
}
