// GraphViewModel.kt
package vc.prog3c.poe.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import vc.prog3c.poe.data.models.GraphBarEntry
import java.util.Calendar

class GraphViewModel : ViewModel() {

    private val _graphData = MutableLiveData<List<GraphBarEntry>>()
    val graphData: LiveData<List<GraphBarEntry>> get() = _graphData

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun loadGraphData(accountId: String, days: Int) {
        val userId = auth.currentUser?.uid ?: return
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val startDate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -days)
                }.time

                db.collection("users")
                    .document(userId)
                    .collection("accounts")
                    .document(accountId)
                    .collection("transactions")
                    .whereGreaterThanOrEqualTo("date", Timestamp(startDate))
                    .get()
                    .addOnSuccessListener { transactionDocs ->

                        val categoryTotals = mutableMapOf<String, Double>()

                        for (doc in transactionDocs) {
                            val category = doc.getString("category") ?: continue
                            val amount = doc.getDouble("amount") ?: 0.0
                            val type = doc.getString("type") ?: "EXPENSE"
                            if (type == "EXPENSE") {
                                categoryTotals[category] = categoryTotals.getOrDefault(category, 0.0) + amount
                            }
                        }

                        db.collection("users")
                            .document(userId)
                            .collection("categories")
                            .get()
                            .addOnSuccessListener { categoryDocs ->
                                val entries = categoryTotals.map { (cat, total) ->
                                    val categoryDoc = categoryDocs.find { it.getString("name")?.equals(cat, ignoreCase = true) == true }
                                    val min = categoryDoc?.getDouble("minGoal") ?: 0.0
                                    val max = categoryDoc?.getDouble("maxGoal") ?: 0.0
                                    GraphBarEntry(category = cat, totalSpent = total, minGoal = min, maxGoal = max)
                                }
                                _graphData.value = entries
                                _isLoading.value = false
                            }
                            .addOnFailureListener {
                                _error.value = "Failed to load category goals"
                                _isLoading.value = false
                            }

                    }
                    .addOnFailureListener {
                        _error.value = "Failed to load transactions"
                        _isLoading.value = false
                    }

            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }
}
