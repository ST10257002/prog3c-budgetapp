package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import vc.prog3c.poe.data.models.Transaction

class GraphViewModel : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Placeholder LiveData for graph data (e.g., income/expense over time)
    private val _incomeExpenseData = MutableLiveData<Map<Long, Pair<Double, Double>>>()
    val incomeExpenseData: LiveData<Map<Long, Pair<Double, Double>>> = _incomeExpenseData

    // Placeholder LiveData for totals if needed for display
    private val _totalIncome = MutableLiveData<Double>()
    val totalIncome: LiveData<Double> = _totalIncome

    private val _totalExpenses = MutableLiveData<Double>()
    val totalExpenses: LiveData<Double> = _totalExpenses

    init {
        // loadGraphData() // Uncomment when ready to load data
    }

    fun loadGraphData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                // TODO: Implement actual graph data loading logic
                delay(1000) // Simulate data loading
                // _incomeExpenseData.value = mapOf(...) // Update with fetched data
                // _totalIncome.value = ...
                // _totalExpenses.value = ...
            } catch (e: Exception) {
                _error.value = "Failed to load graph data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshGraphData() {
        loadGraphData()
    }

    // TODO: Add functions for filtering graph data by time period, category, etc.
}