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
import kotlinx.coroutines.tasks.await
import vc.prog3c.poe.data.models.Category
import vc.prog3c.poe.data.models.GraphBarEntry
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType

class GraphViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _graphEntries = MutableLiveData<List<GraphBarEntry>>()
    val graphEntries: LiveData<List<GraphBarEntry>> = _graphEntries

    private val _totalIncome = MutableLiveData<Double>()
    val totalIncome: LiveData<Double> = _totalIncome

    private val _totalExpenses = MutableLiveData<Double>()
    val totalExpenses: LiveData<Double> = _totalExpenses

    fun loadGraphData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val userId = auth.currentUser?.uid ?: throw Exception("Not logged in")
                Log.d("GRAPH_TEST", "User ID: $userId")

                val categories = fetchCategories(userId)
                Log.d("GRAPH_TEST", "Fetched ${categories.size} categories")

                val transactions = fetchAllTransactions(userId)
                Log.d("GRAPH_TEST", "Fetched ${transactions.size} total transactions")

                val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
                val income = transactions.filter { it.type == TransactionType.INCOME }

                _totalIncome.value = income.sumOf { it.amount }
                _totalExpenses.value = expenses.sumOf { it.amount }

                Log.d("GRAPH_TEST", "Total Income: ${_totalIncome.value}, Total Expenses: ${_totalExpenses.value}")

                _graphEntries.value = buildGraphEntries(categories, expenses)

            } catch (e: Exception) {
                _error.value = e.message
                Log.e("GRAPH_TEST", "Failure loading graph data: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun fetchCategories(userId: String): List<Category> {
        val snapshot = db.collection("users")
            .document(userId)
            .collection("categories")
            .get()
            .await()
        return snapshot.toObjects(Category::class.java)
    }

    private suspend fun fetchAllTransactions(userId: String): List<Transaction> {
        val accounts = db.collection("users").document(userId).collection("accounts").get().await()
        val allTransactions = mutableListOf<Transaction>()

        for (account in accounts) {
            val txSnapshot = db.collection("users")
                .document(userId)
                .collection("accounts")
                .document(account.id)
                .collection("transactions")
                .get()
                .await()

            allTransactions.addAll(txSnapshot.toObjects(Transaction::class.java))
        }

        Log.d("GRAPH_TEST", "Fetched ${allTransactions.size} transactions from accounts")
        return allTransactions
    }

    private fun buildGraphEntries(
        categories: List<Category>,
        transactions: List<Transaction>
    ): List<GraphBarEntry> {
        val categoryIdMap = categories.associateBy { it.id }
        val categoryNameMap = categories.associateBy { it.name }

        val grouped = mutableMapOf<String, Double>()
        for (tx in transactions.filter { it.type == TransactionType.EXPENSE }) {
            val catKey = tx.category
            Log.d("GRAPH_TEST", "Checking transaction category: $catKey")

            val matchedCategory = categoryIdMap[catKey] ?: categoryNameMap[catKey]

            if (matchedCategory != null) {
                val current = grouped.getOrDefault(matchedCategory.id, 0.0)
                grouped[matchedCategory.id] = current + tx.amount
            } else {
                Log.w("GRAPH_TEST", "Unmatched transaction category: $catKey")
            }
        }



        Log.d("GRAPH_TEST", "Built grouped totals for ${grouped.size} categories")

        val result = categories.map { category ->
            GraphBarEntry(
                categoryId = category.id,
                categoryName = category.name,
                totalSpent = grouped[category.id] ?: 0.0,
                minBudget = category.minBudget,
                maxBudget = category.maxBudget
            ).also {
                Log.d("GRAPH_TEST", "Graph Entry: ${it.categoryName} | Spent: ${it.totalSpent}, Min: ${it.minBudget}, Max: ${it.maxBudget}")
            }
        }

        return result
    }
}
