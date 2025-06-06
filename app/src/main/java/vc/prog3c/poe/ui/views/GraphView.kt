package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import vc.prog3c.poe.R
import vc.prog3c.poe.data.models.Category
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import java.util.*

class GraphView : AppCompatActivity() {

    private lateinit var chart: BarChart
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("GRAPH_TEST", "onCreate started")
        setContentView(R.layout.activity_graph)
        //chart = findViewById(R.id.incomeExpenseLineChart)
        chart = findViewById(R.id.categoryBudgetBarChart)

        Log.d("GRAPH_TEST", "GraphView initialized")
        loadGraphData()
        setupBottomNavigation()
    }

    private fun loadGraphData() = lifecycleScope.launch {
        val userId = auth.currentUser?.uid ?: return@launch showError("Not logged in").also {
            Log.e("GRAPH_TEST", "No user logged in")
        }

        try {
            Log.d("GRAPH_TEST", "Loading data for userId: $userId")
            val categories = fetchCategories(userId)
            Log.d("GRAPH_TEST", "Fetched ${categories.size} categories")

            val transactions = fetchTransactions(userId)
            Log.d("GRAPH_TEST", "Fetched ${transactions.size} transactions")

            val dateFiltered = filterByDate(transactions, daysBack = 30)
            Log.d("GRAPH_TEST", "Filtered to ${dateFiltered.size} transactions in last 30 days")

            val groupedSpending = dateFiltered
                .filter { it.type == TransactionType.EXPENSE }
                .groupBy { it.category }

            val entriesActual = ArrayList<BarEntry>()
            val entriesMin = ArrayList<BarEntry>()
            val entriesMax = ArrayList<BarEntry>()
            val labels = ArrayList<String>()

            var index = 0f
            for (category in categories) {
                val totalSpent = groupedSpending[category.name]?.sumOf { it.amount } ?: 0.0
                Log.d(
                    "GRAPH_TEST",
                    "Category: ${category.name}, Spent: $totalSpent, Min: ${category.minBudget}, Max: ${category.maxBudget}"
                )

                entriesActual.add(BarEntry(index, totalSpent.toFloat()))
                entriesMin.add(BarEntry(index, category.minBudget.toFloat()))
                entriesMax.add(BarEntry(index, category.maxBudget.toFloat()))
                labels.add(category.name)
                index += 1f
            }

            val actualData = BarDataSet(entriesActual, "Actual Spending").apply {
                color = ColorTemplate.MATERIAL_COLORS[0]
            }
            val minData = BarDataSet(entriesMin, "Min Budget").apply {
                color = ColorTemplate.MATERIAL_COLORS[1]
            }
            val maxData = BarDataSet(entriesMax, "Max Budget").apply {
                color = ColorTemplate.MATERIAL_COLORS[2]
            }

            val barData = BarData(actualData, minData, maxData).apply {
                barWidth = 0.2f
            }

            chart.data = barData
            chart.description.isEnabled = false
            chart.setVisibleXRangeMaximum(5f)
            chart.groupBars(0f, 0.4f, 0.05f)

            chart.xAxis.apply {
                granularity = 1f
                isGranularityEnabled = true
                valueFormatter = IndexAxisValueFormatter(labels)
                setCenterAxisLabels(true)
                position = XAxis.XAxisPosition.BOTTOM
            }

            chart.axisLeft.axisMinimum = 0f
            chart.axisRight.isEnabled = false
            chart.legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP

            chart.invalidate()
            Log.d("GRAPH_TEST", "Chart rendered successfully")

        } catch (e: Exception) {
            showError("Failed to load graph: ${e.message}")
            Log.e("GRAPH_TEST", "Graph loading failed: ${e.message}", e)
        }
    }

    private suspend fun fetchTransactions(userId: String): List<Transaction> {
        val accountsSnapshot =
            db.collection("users").document(userId).collection("accounts").get().await()
        val transactions = mutableListOf<Transaction>()

        for (doc in accountsSnapshot) {
            val accountId = doc.id
            val txSnapshot = db.collection("users")
                .document(userId)
                .collection("accounts")
                .document(accountId)
                .collection("transactions")
                .get()
                .await()

            transactions.addAll(txSnapshot.toObjects(Transaction::class.java))
        }

        Log.d("GRAPH_TEST", "Total transactions fetched from all accounts: ${transactions.size}")
        return transactions
    }

    private suspend fun fetchCategories(userId: String): List<Category> {
        val snapshot: QuerySnapshot = db.collection("users")
            .document(userId)
            .collection("categories")
            .get()
            .await()
        return snapshot.toObjects(Category::class.java)
    }

    private fun filterByDate(transactions: List<Transaction>, daysBack: Int): List<Transaction> {
        val cutoff = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -daysBack)
        }.time

        return transactions.filter { it.date.toDate().after(cutoff) }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, DashboardView::class.java))
                    true
                }
                R.id.nav_accounts -> {
                    startActivity(Intent(this, AccountsView::class.java))
                    true
                }
                R.id.nav_graph -> true // already on this screen
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
        bottomNav.selectedItemId = R.id.nav_graph
    }
}
