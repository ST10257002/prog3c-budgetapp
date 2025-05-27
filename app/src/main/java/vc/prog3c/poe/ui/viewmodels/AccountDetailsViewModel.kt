package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import vc.prog3c.poe.data.models.Account // Correct import
import vc.prog3c.poe.data.models.Transaction // Correct import
import vc.prog3c.poe.data.models.TransactionType // Correct import
import java.util.Date
import java.util.UUID

class AccountDetailsViewModel : ViewModel() {
    private val _account = MutableLiveData<Account?>()
    val account: LiveData<Account?> = _account

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var currentAccount: Account? = null // Store the currently loaded account


    fun loadAccountDetails(accountId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // TODO: Replace with actual data loading from repository/service
                // Simulate fetching account details
                val fetchedAccount = getMockAccount(accountId)
                _account.value = fetchedAccount
                currentAccount = fetchedAccount

                if (fetchedAccount != null) {
                    // Simulate fetching transactions for the account
                    val fetchedTransactions = getMockTransactionsForAccount(accountId)
                    _transactions.value = fetchedTransactions
                } else {
                    _transactions.value = emptyList()
                }

            } catch (e: Exception) {
                _error.value = "Failed to load account details: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterTransactionsByTimePeriod(period: String) {
        // TODO: Implement actual filtering logic based on date and period
        // For now, this is a placeholder
        val filteredList = when (period) {
            "1 week" -> _transactions.value?.filter { it.date.time >= System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L }
            "1 month" -> _transactions.value?.filter { it.date.time >= System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000L }
            "3 months" -> _transactions.value?.filter { it.date.time >= System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000L }
            else -> _transactions.value
        }
        _transactions.value = filteredList ?: emptyList() // Update displayed transactions
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                currentAccount?.let { account ->
                    // TODO: Implement actual account deletion via repository/service
                    delay(1000) // Simulate deletion process
                    // After successful deletion, navigate back or update UI
                    _account.value = null // Clear account details
                    _transactions.value = emptyList() // Clear transactions
                    // Post a success message or event
                } ?: run {
                    _error.value = "No account selected to delete."
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete account: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    // Mock data functions (replace with actual service/repository calls)
    private fun getMockAccount(accountId: String): Account? {
        // Simulate fetching a specific account
        val allMockAccounts = listOf(
            Account(id = "account1", userId = "user1", name = "Checking", type = "Debit", balance = 15000.0, transactionsCount = 2),
            Account(id = "account2", userId = "user1", name = "Savings", type = "Savings", balance = 35000.0, transactionsCount = 2)
        )
        return allMockAccounts.find { it.id == accountId }
    }

    private fun getMockTransactionsForAccount(accountId: String): List<Transaction> {
        // Simulate fetching transactions for a specific account
        val allMockTransactions = listOf(
            Transaction(id = "trans1", userId = "user1", accountId = "account1", type = TransactionType.INCOME, amount = 20000.0, category = "Salary", date = Date(System.currentTimeMillis() - 86400000L * 5), description = "Monthly Salary"),
            Transaction(id = "trans2", userId = "user1", accountId = "account1", type = TransactionType.EXPENSE, amount = 5000.0, category = "Groceries", date = Date(System.currentTimeMillis() - 86400000L * 3), description = "Weekly Groceries"),
            Transaction(id = "trans3", userId = "user1", accountId = "account2", type = TransactionType.INCOME, amount = 10000.0, category = "Transfer", date = Date(System.currentTimeMillis() - 86400000L * 2), description = "Transfer from Checking"),
            Transaction(id = "trans4", userId = "user1", accountId = "account2", type = TransactionType.EXPENSE, amount = 2000.0, category = "Investment", date = Date(System.currentTimeMillis() - 86400000L * 1), description = "Buy Stocks")
        )
        return allMockTransactions.filter { it.accountId == accountId }.sortedByDescending { it.date }
    }
}