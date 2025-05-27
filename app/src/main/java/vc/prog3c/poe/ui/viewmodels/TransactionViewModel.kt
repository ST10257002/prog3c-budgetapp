package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import vc.prog3c.poe.data.repository.TransactionRepository
import java.util.Date

class TransactionViewModel : ViewModel() {

    private val repo = TransactionRepository()

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private var _allTransactions = listOf<Transaction>()
    //private var _allTransactions = MutableLiveData<List<Transaction>>() // full list

    private val _totalIncome = MutableLiveData<Double>()
    val totalIncome: LiveData<Double> = _totalIncome

    private val _totalExpenses = MutableLiveData<Double>()
    val totalExpenses: LiveData<Double> = _totalExpenses

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /*
    fun loadTransactions(accountId: String?) {
        _isLoading.value = true

        repo.getAllTransactions(accountId) { result ->
            _allTransactions.postValue(result)
            _transactions.postValue(result) // initial unfiltered list
            _totalIncome.postValue(result.filter { it.type == TransactionType.INCOME }.sumOf { it.amount })
            _totalExpenses.postValue(result.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount })
            _isLoading.postValue(false)
        }
    }
    */

    fun loadTransactions(accountId: String? = null) {
        _isLoading.value = true

        // Bypass account filtering by passing null
        repo.getAllTransactions(null) { all ->
            _allTransactions = all

            _transactions.postValue(all)
            _totalIncome.postValue(all.filter { it.type == TransactionType.INCOME }.sumOf { it.amount })
            _totalExpenses.postValue(all.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount })
            _isLoading.postValue(false)
        }
    }


    fun refreshTransactions(accountId: String? = null) {
        loadTransactions(accountId)
    }

    private fun updateTotals(transactions: List<Transaction>) {
        val incomeTotal = transactions.filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
        val expenseTotal = transactions.filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        _totalIncome.value = incomeTotal
        _totalExpenses.value = expenseTotal
    }

/*
    fun filterTransactionsByType(type: TransactionType, accountId: String?) {
        val filtered = _allTransactions.value?.filter {
            (type == TransactionType.ALL || it.type == type) &&
                    (accountId == null || it.accountId == accountId)
        } ?: emptyList()

        _transactions.postValue(filtered)
    }
*/

    fun filterTransactionsByType(type: TransactionType, accountId: String?) {
        val filtered = _allTransactions.filter {
            (type == TransactionType.ALL || it.type == type) &&
                    (accountId == null || it.accountId == accountId)
        }

        _transactions.postValue(filtered)
    }


    fun getTransactionsByDateRange(startDate: Date, endDate: Date): List<Transaction> {
        return _transactions.value?.filter { it.date in startDate..endDate } ?: emptyList()
    }

    fun getTransactionsByCategory(category: String): List<Transaction> {
        return _transactions.value?.filter { it.category == category } ?: emptyList()
    }

    fun getTotalForCategory(category: String): Double {
        return getTransactionsByCategory(category).sumOf { it.amount }
    }
}
