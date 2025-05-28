package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import vc.prog3c.poe.data.models.Account
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import vc.prog3c.poe.data.repository.AccountRepository

class AccountDetailsViewModel(
    private val repo: AccountRepository = AccountRepository()
) : ViewModel() {

    private val _account = MutableLiveData<Account?>()
    val account: LiveData<Account?> = _account

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _calculatedBalance = MutableLiveData<Double>()
    val calculatedBalance: LiveData<Double> = _calculatedBalance

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var allTxs: List<Transaction> = emptyList()

    fun loadAccountDetails(accountId: String) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null

        val accountResult = repo.getAccount(accountId)
        val txResult = repo.getTransactionsForAccount(accountId)

        accountResult
            .onSuccess { _account.value = it }
            .onFailure { _error.value = it.localizedMessage }

        txResult
            .onSuccess { txs ->
                allTxs = txs
                _transactions.value = txs
                _calculatedBalance.value = txs.fold(0.0) { acc, tx ->
                    when (tx.type) {
                        TransactionType.INCOME -> acc + tx.amount
                        TransactionType.EXPENSE -> acc - tx.amount
                    }
                }
            }
            .onFailure { _error.value = it.localizedMessage }

        _isLoading.value = false
    }

    fun filterTransactionsByTimePeriod(period: String) {
        val now = System.currentTimeMillis()
        val cutoff = when (period) {
            "1 week" -> now - 7L * 24 * 60 * 60 * 1000
            "1 month" -> now - 30L * 24 * 60 * 60 * 1000
            "3 months" -> now - 90L * 24 * 60 * 60 * 1000
            else -> 0L
        }
        _transactions.value = allTxs.filter { it.date.toDate().time >= cutoff }
    }

    fun deleteAccount(accountId: String) = viewModelScope.launch {
        _isLoading.value = true
        val result = repo.deleteAccount(accountId)
        if (result.isFailure) _error.value = result.exceptionOrNull()?.localizedMessage
        _isLoading.value = false
    }
}
