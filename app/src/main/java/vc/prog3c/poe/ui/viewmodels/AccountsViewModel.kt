package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import vc.prog3c.poe.data.models.Account
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import vc.prog3c.poe.data.repository.AccountRepository

class AccountsViewModel : ViewModel() {
    private val repo = AccountRepository()

    private val _accounts = MutableLiveData<List<Account>>()
    val accounts: LiveData<List<Account>> = _accounts

    private val _netWorth = MutableLiveData<Double>()
    val netWorth: LiveData<Double> = _netWorth

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        fetchAccounts()
    }

    fun fetchAccounts() {
        _isLoading.value = true
        repo.getAllAccounts { accountList ->
            if (accountList.isEmpty()) {
                _accounts.postValue(emptyList())
                _netWorth.postValue(0.0)
                _isLoading.postValue(false)
                return@getAllAccounts
            }

            // For each account, fetch transactions and update balance/count
            val updatedAccounts = mutableListOf<Account>()
            var completed = 0

            accountList.forEach { account ->
                repo.getTransactionsForAccount(account.id) { txs ->
                    // Compute balance and count
                    val balance = calculateBalance(txs)
                    account.balance = balance
                    account.transactionsCount = txs.size

                    updatedAccounts.add(account)
                    completed++

                    // When all accounts have been processed, post the results
                    if (completed == accountList.size) {
                        _accounts.postValue(updatedAccounts)
                        _netWorth.postValue(updatedAccounts.sumOf { it.balance })
                        _isLoading.postValue(false)
                    }
                }
            }
        }
    }

    private fun calculateBalance(transactions: List<Transaction>): Double {
        return transactions.sumOf { transaction ->
            when (transaction.type) {
                TransactionType.INCOME -> transaction.amount
                TransactionType.EXPENSE -> -transaction.amount
                TransactionType.EARNED -> 0.0
                TransactionType.REDEEMED -> 0.0
            }
        }
    }

    fun addAccount(account: Account) {
        _isLoading.value = true
        repo.addAccount(account) { success ->
            if (success) fetchAccounts()
            else _error.postValue("Failed to add account.")
            _isLoading.postValue(false)
        }
    }

    fun updateAccount(account: Account) {
        _isLoading.value = true
        repo.updateAccount(account) { success ->
            if (success) fetchAccounts()
            else _error.postValue("Failed to update account.")
            _isLoading.postValue(false)
        }
    }

    fun deleteAccount(accountId: String) {
        _isLoading.value = true
        repo.deleteAccount(accountId) { success ->
            if (success) fetchAccounts()
            else _error.postValue("Failed to delete account.")
            _isLoading.postValue(false)
        }
    }
}
