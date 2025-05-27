package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import vc.prog3c.poe.data.models.Income
import vc.prog3c.poe.data.repository.IncomeRepository
import java.util.Date

class IncomeViewModel : ViewModel() {

    private val repo = IncomeRepository()

    private val _incomes = MutableLiveData<List<Income>>()
    val incomes: LiveData<List<Income>> = _incomes

    private val _totalIncome = MutableLiveData<Double>()
    val totalIncome: LiveData<Double> = _totalIncome

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    init {
        fetchIncomes()
    }

    fun fetchIncomes() {
        _isLoading.value = true
        repo.getAllIncomes { result ->
            _incomes.postValue(result)
            _totalIncome.postValue(result.sumOf { it.amount })
            _isLoading.postValue(false)
        }
    }

    fun addIncome(income: Income) {
        _isLoading.value = true
        repo.addIncome(income) { success ->
            _saveSuccess.postValue(success)
            if (success) fetchIncomes()
            else _error.postValue("Failed to add income.")
            _isLoading.postValue(false)
        }
    }

    fun deleteIncome(incomeId: String) {
        _isLoading.value = true
        repo.deleteIncome(incomeId) { success ->
            if (success) fetchIncomes()
            else _error.postValue("Failed to delete income.")
            _isLoading.postValue(false)
        }
    }

    fun getIncomeForPeriod(startDate: Timestamp, endDate: Timestamp): List<Income> {
        return _incomes.value?.filter {
            val incomeDate = it.date.toDate()
            incomeDate.after(startDate.toDate()) || incomeDate == startDate.toDate()
        }?.filter {
            val incomeDate = it.date.toDate()
            incomeDate.before(endDate.toDate()) || incomeDate == endDate.toDate()
        } ?: emptyList()
    }

    fun getTotalForSource(source: String): Double {
        return _incomes.value?.filter { it.source == source }?.sumOf { it.amount } ?: 0.0
    }
}
