package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import vc.prog3c.poe.data.models.Account // Correct import
import java.util.UUID

class AccountsViewModel : ViewModel() {
    private val _accounts = MutableLiveData<List<Account>>()
    val accounts: LiveData<List<Account>> = _accounts

    private val _netWorth = MutableLiveData<Double>()
    val netWorth: LiveData<Double> = _netWorth

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadAccounts()
    }

    fun loadAccounts() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                // TODO: Replace with actual data loading from repository/service
                delay(1000) // Simulate network delay
                val fetchedAccounts = getMockAccounts() // Simulate fetching accounts
                _accounts.value = fetchedAccounts
                updateNetWorth(fetchedAccounts)
            } catch (e: Exception) {
                _error.value = "Failed to load accounts: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addAccount(account: Account) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                // TODO: Implement actual account adding via repository/service
                delay(500) // Simulate adding process

                val currentList = _accounts.value?.toMutableList() ?: mutableListOf()
                currentList.add(account)
                _accounts.value = currentList // Update LiveData
                updateNetWorth(currentList)
            } catch (e: Exception) {
                _error.value = "Failed to add account: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAccount(account: Account) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                // TODO: Implement actual account update via repository/service
                delay(500) // Simulate update process

                val currentList = _accounts.value?.toMutableList() ?: return@launch
                val index = currentList.indexOfFirst { it.id == account.id }
                if (index != -1) {
                    currentList[index] = account
                    _accounts.value = currentList // Update LiveData
                    updateNetWorth(currentList)
                } else {
                    _error.value = "Account not found for update."
                }
            } catch (e: Exception) {
                _error.value = "Failed to update account: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAccount(accountId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                // TODO: Implement actual account deletion via repository/service
                delay(500) // Simulate deletion process

                val currentList = _accounts.value?.toMutableList() ?: return@launch
                val removed = currentList.removeIf { it.id == accountId }
                if (removed) {
                    _accounts.value = currentList // Update LiveData
                    updateNetWorth(currentList)
                } else {
                    _error.value = "Account not found for deletion."
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete account: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun updateNetWorth(accounts: List<Account>) {
        val total = accounts.sumOf { it.balance }
        _netWorth.value = total
    }


    // Mock data function (replace with actual service/repository call)
    private fun getMockAccounts(): List<Account> {
        // Simulate fetching accounts for a user
        val testUserId = "user1" // Replace with actual logged-in user ID
        return listOf(
            Account(id = "account1", userId = testUserId, name = "Checking", type = "Debit", balance = 15000.0, transactionsCount = 2),
            Account(id = "account2", userId = testUserId, name = "Savings", type = "Savings", balance = 35000.0, transactionsCount = 2),
            Account(id = "account3", userId = testUserId, name = "Credit Card", type = "Credit", balance = -2000.0, transactionsCount = 5)
        )
    }
}