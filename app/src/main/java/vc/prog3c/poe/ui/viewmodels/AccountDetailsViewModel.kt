package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import vc.prog3c.poe.data.models.Account
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.repository.AccountRepository
import vc.prog3c.poe.data.models.TransactionType
import java.util.Date
import java.util.UUID

class AccountDetailsViewModel(
    private val repo: AccountRepository = AccountRepository()
) : ViewModel() {

    private val _account      = MutableLiveData<Account?>()
    val account: LiveData<Account?> = _account

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _isLoading    = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error        = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var allTxs: List<Transaction> = emptyList()

    /** ← now needs userId too */
    fun loadAccountDetails(userId: String, accountId: String) {
        _isLoading.value = true
        _error.value = null

        // 1) load the account document (with its stored aggregates)
        repo.getAccount(accountId) { acct ->
            _account.postValue(acct)

            // 2) load all txs (we’ll filter later)
            repo.getTransactionsForAccount(accountId) { txs ->
                allTxs = txs
                _transactions.postValue(txs)
                _isLoading.postValue(false)
            }
        }
    }

    fun filterTransactionsByTimePeriod(period: String) {
        val now = System.currentTimeMillis()
        val cutoff = when (period) {
            "1 week"  -> now - 7L * 24*60*60*1000
            "1 month" -> now - 30L*24*60*60*1000
            "3 months"-> now - 90L*24*60*60*1000
            else      -> 0L
        }
        _transactions.value = allTxs.filter { it.date.toDate().time >= cutoff }
    }

    fun deleteAccount(accountId: String) {
        _isLoading.value = true
        _error.value = null
        repo.deleteAccount(accountId) { success ->
            if (!success) _error.postValue("Failed to delete account.")
            _isLoading.postValue(false)
        }
    }
}
