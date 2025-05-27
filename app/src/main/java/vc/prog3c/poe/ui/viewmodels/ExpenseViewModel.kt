package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import vc.prog3c.poe.data.models.Expense
import vc.prog3c.poe.data.repository.ExpenseRepository
import java.text.SimpleDateFormat
import java.util.*

class ExpenseViewModel : ViewModel() {

    private val _expenses = MutableLiveData<List<Expense>>()
    val expenses: LiveData<List<Expense>> = _expenses

    private val _totalExpenses = MutableLiveData<Double>()
    val totalExpenses: LiveData<Double> = _totalExpenses

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private val repo = ExpenseRepository()

    init {
        fetchExpenses()
    }

    fun fetchExpenses() {
        _isLoading.value = true
        repo.getAllExpenses { list ->
            _expenses.postValue(list)
            _totalExpenses.postValue(list.sumOf { it.amount })
            _isLoading.postValue(false)
        }
    }

    fun addExpense(expense: Expense) {
        _isLoading.value = true
        repo.addExpense(expense) { success ->
            _saveSuccess.postValue(success)
            if (success) fetchExpenses()
            else _error.postValue("Failed to save expense.")
            _isLoading.postValue(false)
        }
    }

    // Utility filters
    fun getExpensesByCategory(categoryId: String): List<Expense> {
        return _expenses.value?.filter { it.categoryId == categoryId } ?: emptyList()
    }

    fun getExpensesForPeriod(start: String, end: String): List<Expense> {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDate = formatter.parse(start)
        val endDate = formatter.parse(end)

        return _expenses.value?.filter {
            val expenseDate = it.date.toDate()
            expenseDate != null && expenseDate >= startDate && expenseDate <= endDate
        } ?: emptyList()
    }


    fun getTotalForCategory(categoryId: String): Double {
        return getExpensesByCategory(categoryId).sumOf { it.amount }
    }

    fun updateExpense(expenseId: String, updatedExpense: Expense) {
        _isLoading.value = true
        repo.updateExpense(expenseId, updatedExpense) { success ->
            _saveSuccess.postValue(success)
            if (success) fetchExpenses()
            else _error.postValue("Failed to update expense")
            _isLoading.postValue(false)
        }
    }

    fun deleteExpense(expenseId: String) {
        _isLoading.value = true
        repo.deleteExpense(expenseId) { success ->
            _saveSuccess.postValue(success)
            if (success) fetchExpenses()
            else _error.postValue("Failed to delete expense")
            _isLoading.postValue(false)
        }
    }
}
