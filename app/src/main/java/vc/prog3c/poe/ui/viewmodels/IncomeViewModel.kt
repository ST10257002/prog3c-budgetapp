package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

    init {
        loadTestData()
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
        // TODO: Implement Firestore income addition
        // - Add to Firestore collection
        // - Handle offline persistence
        // - Implement error handling
        val currentList = _incomes.value?.toMutableList() ?: mutableListOf()
        currentList.add(income)
        _incomes.value = currentList
        updateTotalIncome()
    }

    fun updateIncome(income: Income) {
        // TODO: Implement Firestore income update
        // - Update Firestore document
        // - Handle offline persistence
        // - Implement error handling
        val currentList = _incomes.value?.toMutableList() ?: return
        val index = currentList.indexOfFirst { it.id == income.id }
        if (index != -1) {
            currentList[index] = income
            _incomes.value = currentList
            updateTotalIncome()
        }
    }

    fun deleteIncome(incomeId: String) {
        // TODO: Implement Firestore income deletion
        // - Delete from Firestore collection
        // - Handle offline persistence
        // - Implement error handling
        val currentList = _incomes.value?.toMutableList() ?: return
        currentList.removeIf { it.id == incomeId }
        _incomes.value = currentList
        updateTotalIncome()
    }

    fun getIncomeForPeriod(startDate: Date, endDate: Date): List<Income> {
        return _incomes.value?.filter { it.date in startDate..endDate } ?: emptyList()
    }
} 