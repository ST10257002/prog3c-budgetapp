package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

data class Income(
    val id: String,
    val amount: Double,
    val source: String,
    val date: Date,
    val description: String? = null
)

class IncomeViewModel : ViewModel() {
    // TODO: Replace with Firestore implementation
    // - Create Firestore collection for income
    // - Implement real-time listeners for income updates
    // - Add offline persistence support
    // - Implement data synchronization
    // - Add error handling for network issues

    private val _incomes = MutableLiveData<List<Income>>()
    val incomes: LiveData<List<Income>> = _incomes

    private val _totalIncome = MutableLiveData<Double>()
    val totalIncome: LiveData<Double> = _totalIncome

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private var lastOperation: (() -> Unit)? = null

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // TODO: Backend Implementation Required
                // - Fetch incomes from Firestore
                // - Handle offline state
                // - Implement error handling
                delay(1000) // Simulate network delay
                loadTestData()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to load incomes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadTestData() {
        // Test data for income
        _incomes.value = listOf(
            Income(
                id = "1",
                amount = 25000.0,
                source = "Salary",
                date = Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000), // 7 days ago
                description = "Monthly salary"
            ),
            Income(
                id = "2",
                amount = 5000.0,
                source = "Freelance",
                date = Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000), // 3 days ago
                description = "Web development project"
            ),
            Income(
                id = "3",
                amount = 2000.0,
                source = "Investments",
                date = Date(System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000), // 1 day ago
                description = "Stock dividends"
            )
        )

        updateTotalIncome()
    }

    private fun updateTotalIncome() {
        _totalIncome.value = _incomes.value?.sumOf { it.amount } ?: 0.0
    }

    fun addIncome(income: Income) {
        lastOperation = { addIncome(income) }
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // TODO: Backend Implementation Required
                // - Add to Firestore collection
                // - Handle offline persistence
                // - Implement error handling
                delay(1000) // Simulate network delay
                val currentList = _incomes.value?.toMutableList() ?: mutableListOf()
                currentList.add(income)
                _incomes.value = currentList
                updateTotalIncome()
                _saveSuccess.value = true
                _error.value = null
            } catch (e: Exception) {
                _error.value = when (e) {
                    is IllegalArgumentException -> "Invalid income data: ${e.message}"
                    is IllegalStateException -> "Failed to save income: ${e.message}"
                    else -> "An unexpected error occurred: ${e.message}"
                }
                _saveSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun retryLastOperation() {
        lastOperation?.invoke()
    }

    fun updateIncome(income: Income) {
        lastOperation = { updateIncome(income) }
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // TODO: Backend Implementation Required
                // - Update Firestore document
                // - Handle offline persistence
                // - Implement error handling
                delay(1000) // Simulate network delay
                val currentList = _incomes.value?.toMutableList() ?: return@launch
                val index = currentList.indexOfFirst { it.id == income.id }
                if (index != -1) {
                    currentList[index] = income
                    _incomes.value = currentList
                    updateTotalIncome()
                    _saveSuccess.value = true
                    _error.value = null
                }
            } catch (e: Exception) {
                _error.value = when (e) {
                    is IllegalArgumentException -> "Invalid income data: ${e.message}"
                    is IllegalStateException -> "Failed to update income: ${e.message}"
                    else -> "An unexpected error occurred: ${e.message}"
                }
                _saveSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteIncome(incomeId: String) {
        lastOperation = { deleteIncome(incomeId) }
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // TODO: Backend Implementation Required
                // - Delete from Firestore collection
                // - Handle offline persistence
                // - Implement error handling
                delay(1000) // Simulate network delay
                val currentList = _incomes.value?.toMutableList() ?: return@launch
                currentList.removeIf { it.id == incomeId }
                _incomes.value = currentList
                updateTotalIncome()
                _saveSuccess.value = true
                _error.value = null
            } catch (e: Exception) {
                _error.value = when (e) {
                    is IllegalArgumentException -> "Invalid income ID: ${e.message}"
                    is IllegalStateException -> "Failed to delete income: ${e.message}"
                    else -> "An unexpected error occurred: ${e.message}"
                }
                _saveSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getIncomeForPeriod(startDate: Date, endDate: Date): List<Income> {
        return _incomes.value?.filter { it.date in startDate..endDate } ?: emptyList()
    }
} 