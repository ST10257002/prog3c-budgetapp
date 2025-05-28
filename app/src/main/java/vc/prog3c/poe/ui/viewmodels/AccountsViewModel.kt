package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import vc.prog3c.poe.data.models.Account
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
        repo.getAllAccounts { list ->
            _accounts.postValue(list)
            _netWorth.postValue(list.sumOf { it.balance })
            _isLoading.postValue(false)
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
