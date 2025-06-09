package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vc.prog3c.poe.data.models.Account
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import vc.prog3c.poe.data.repository.AccountRepository
import vc.prog3c.poe.ui.viewmodels.AccountsUiState.Failure
import vc.prog3c.poe.ui.viewmodels.AccountsUiState.Loading
import vc.prog3c.poe.ui.viewmodels.AccountsUiState.Updated
/**
 * @reference Firebase Firestore - Reading Documents in Batches: https://firebase.google.com/docs/firestore/query-data/get-data
 * @reference Android MutableLiveData: https://developer.android.com/reference/androidx/lifecycle/MutableLiveData
 */

class AccountsViewModel(
    private val repository: AccountRepository = AccountRepository()
) : ViewModel() {
    companion object {
        private const val TAG = "AccountsViewModel"
    }


    // --- Fields


    private val _uiState = MutableLiveData<AccountsUiState>()
    val uiState: LiveData<AccountsUiState> = _uiState
    
    
    init {
        fetchAccounts()
    }


    // --- Internals


    fun fetchAccounts() {
        _uiState.value = Loading
        repository.getAllAccounts { accountList ->
            if (accountList.isEmpty()) {
                _uiState.value = Updated(
                    accounts = emptyList(), netWorth = 0.0
                )
                return@getAllAccounts
            }

            // For each account, fetch transactions and update balance/count
            val updatedAccounts = mutableListOf<Account>()
            var completed = 0

            accountList.forEach { account ->
                repository.getTransactionsForAccount(account.id) { txs ->
                    // Compute balance and count
                    val balance = calculateBalance(txs)
                    account.balance = balance
                    account.transactionsCount = txs.size

                    updatedAccounts.add(account)
                    completed++

                    // When all accounts have been processed, post the results
                    if (completed == accountList.size) {
                        _uiState.value = Updated(
                            accounts = updatedAccounts, netWorth = updatedAccounts.sumOf {
                                it.balance
                            })
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
        _uiState.value = Loading
        repository.addAccount(account) { success ->
            if (success) fetchAccounts()
            else _uiState.value = Failure("Failed to create account.")
        }
    }

    
    fun updateAccount(account: Account) {
        _uiState.value = Loading
        repository.updateAccount(account) { success ->
            if (success) fetchAccounts()
            else _uiState.value = Failure("Failed to update account.")
        }
    }

    
    fun deleteAccount(accountId: String) {
        _uiState.value = Loading
        repository.deleteAccount(accountId) { success ->
            if (success) fetchAccounts()
            else _uiState.value = Failure("Failed to delete account.")
        }
    }
}
