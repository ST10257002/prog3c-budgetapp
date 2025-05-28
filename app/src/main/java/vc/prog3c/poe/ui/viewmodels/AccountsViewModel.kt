package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import vc.prog3c.poe.data.models.Account
import vc.prog3c.poe.data.repository.AccountRepository

class AccountsViewModel(
    private val repo: AccountRepository = AccountRepository()
) : ViewModel() {

    private val _accounts = MutableLiveData<List<Account>>()
    val accounts: LiveData<List<Account>> = _accounts

    private val _netWorth = MutableLiveData<Double>()
    val netWorth: LiveData<Double> = _netWorth

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        fetchAccounts()
    }

    fun fetchAccounts() = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null

        val result = repo.getAllAccounts()

        result
            .onSuccess { accountList ->
                _accounts.value = accountList
                _netWorth.value = accountList.sumOf { it.balance }
            }
            .onFailure { e ->
                _error.value = e.message ?: "Failed to fetch accounts"
            }

        _isLoading.value = false
    }

    fun addAccount(account: Account) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null

        val result = repo.addAccount(account)

        result
            .onSuccess { fetchAccounts() }
            .onFailure { e ->
                _error.value = e.message ?: "Failed to add account"
            }

        _isLoading.value = false
    }

    fun addOrUpdateAccount(account: Account) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null

        val result = repo.addOrUpdateAccount(account)

        result
            .onSuccess { fetchAccounts() }
            .onFailure { e ->
                _error.value = e.message ?: "Failed to save account"
            }

        _isLoading.value = false
    }

    fun deleteAccount(accountId: String) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null

        val result = repo.deleteAccount(accountId)

        result
            .onSuccess { fetchAccounts() }
            .onFailure { e ->
                _error.value = e.message ?: "Failed to delete account"
            }

        _isLoading.value = false
    }
}
