package vc.prog3c.poe.ui.views

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import vc.prog3c.poe.R
import vc.prog3c.poe.data.models.Category
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import vc.prog3c.poe.utils.CurrencyFormatter
import vc.prog3c.poe.databinding.ActivityGraphBinding
import java.text.SimpleDateFormat
import java.util.*

class GraphView : AppCompatActivity() {

    private lateinit var binding: ActivityGraphBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private var currentTimePeriod = TimePeriod.MONTH
    private var currentCategoryTimePeriod = TimePeriod.MONTH

    enum class TimePeriod(val days: Int) {
        WEEK(7),
        MONTH(30),
        YEAR(365)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGraphBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupCharts()
        setupTimePeriodChips()
        setupBottomNavigation()
        loadGraphData()
    }

    private fun setupCharts() {
        // Setup Bar Chart
        binding.categoryBudgetBarChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setPinchZoom(false)
            setScaleEnabled(true)
            setDrawBorders(false)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                setCenterAxisLabels(true)
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }
            
            axisRight.isEnabled = false
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
            }
        }

        // Setup Line Chart
        binding.incomeExpenseLineChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBorders(false)
            setTouchEnabled(true)
            setScaleEnabled(true)
            setPinchZoom(true)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }
            
            axisRight.isEnabled = false
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
            }
        }
    }

    private fun setupTimePeriodChips() {
        // Income vs Expenses time period
        binding.chipWeek.setOnClickListener {
            currentTimePeriod = TimePeriod.WEEK
            loadGraphData()
        }
        binding.chipMonth.setOnClickListener {
            currentTimePeriod = TimePeriod.MONTH
            loadGraphData()
        }
        binding.chipYear.setOnClickListener {
            currentTimePeriod = TimePeriod.YEAR
            loadGraphData()
        }

        // Category time period
        binding.chipCategoryWeek.setOnClickListener {
            currentCategoryTimePeriod = TimePeriod.WEEK
            loadGraphData()
        }
        binding.chipCategoryMonth.setOnClickListener {
            currentCategoryTimePeriod = TimePeriod.MONTH
            loadGraphData()
        }
        binding.chipCategoryYear.setOnClickListener {
            currentCategoryTimePeriod = TimePeriod.YEAR
            loadGraphData()
        }
    }

    private fun loadGraphData() = lifecycleScope.launch {
        val userId = auth.currentUser?.uid ?: return@launch showError("Not logged in")
        
        try {
            val transactions = fetchTransactions(userId)
            updateIncomeExpenseChart(transactions)
            updateCategoryBudgetChart(transactions)
        } catch (e: Exception) {
            showError("Failed to load graph: ${e.message}")
            Log.e("GRAPH_TEST", "Graph loading failed", e)
        }
    }

    private fun updateIncomeExpenseChart(transactions: List<Transaction>) {
        val filteredTransactions = filterByDate(transactions, currentTimePeriod.days)
        val sortedTransactions = filteredTransactions.sortedBy { it.date }
        
        val incomeEntries = ArrayList<Entry>()
        val expenseEntries = ArrayList<Entry>()
        val labels = ArrayList<String>()
        
        var index = 0f
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        
        sortedTransactions.forEach { transaction ->
            when (transaction.type) {
                TransactionType.INCOME -> {
                    incomeEntries.add(Entry(index, transaction.amount.toFloat()))
                }
                TransactionType.EXPENSE -> {
                    expenseEntries.add(Entry(index, transaction.amount.toFloat()))
                }
                else -> {}
            }
            labels.add(dateFormat.format(transaction.date.toDate()))
            index += 1f
        }

        val incomeDataSet = LineDataSet(incomeEntries, "Income").apply {
            color = Color.GREEN
            setCircleColor(Color.GREEN)
            lineWidth = 2f
            setDrawValues(false)
        }

        val expenseDataSet = LineDataSet(expenseEntries, "Expenses").apply {
            color = Color.RED
            setCircleColor(Color.RED)
            lineWidth = 2f
            setDrawValues(false)
        }

        binding.incomeExpenseLineChart.data = LineData(incomeDataSet, expenseDataSet)
        binding.incomeExpenseLineChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.incomeExpenseLineChart.invalidate()

        // Update summary texts
        val totalIncome = filteredTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpenses = filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val balance = totalIncome - totalExpenses

        binding.totalIncomeText.text = CurrencyFormatter.format(totalIncome)
        binding.totalExpensesText.text = CurrencyFormatter.format(totalExpenses)
        binding.balanceText.text = CurrencyFormatter.format(balance)
    }

    private suspend fun updateCategoryBudgetChart(transactions: List<Transaction>) {
        val filteredTransactions = filterByDate(transactions, currentCategoryTimePeriod.days)
        val categories = fetchCategories(auth.currentUser?.uid ?: return)
        
        val entriesActual = ArrayList<BarEntry>()
        val entriesMin = ArrayList<BarEntry>()
        val entriesMax = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        var index = 0f
        for (category in categories) {
            val totalSpent = filteredTransactions
                .filter { it.type == TransactionType.EXPENSE && it.category == category.name }
                .sumOf { it.amount }

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

        binding.categoryBudgetBarChart.data = barData
        binding.categoryBudgetBarChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.categoryBudgetBarChart.groupBars(0f, 0.4f, 0.05f)
        binding.categoryBudgetBarChart.invalidate()
    }

    private suspend fun fetchTransactions(userId: String): List<Transaction> {
        val accountsSnapshot = db.collection("users").document(userId).collection("accounts").get().await()
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
        binding.bottomNavigation.setOnItemSelectedListener { item ->
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
        binding.bottomNavigation.selectedItemId = R.id.nav_graph
    }
}
