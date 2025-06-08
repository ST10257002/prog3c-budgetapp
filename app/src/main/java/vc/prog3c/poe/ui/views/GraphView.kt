package vc.prog3c.poe.ui.views

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import vc.prog3c.poe.R
import vc.prog3c.poe.core.services.AuthService
import vc.prog3c.poe.core.utils.CurrencyFormatter
import vc.prog3c.poe.data.models.Category
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import vc.prog3c.poe.databinding.ActivityGraphBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class GraphView : AppCompatActivity(), View.OnClickListener {

    private lateinit var binds: ActivityGraphBinding

    private val dataService = FirebaseFirestore.getInstance()
    private val authService = AuthService()

    private var currentTimePeriod = TimePeriod.MONTH
    private var currentCategoryTimePeriod = TimePeriod.MONTH

    enum class TimePeriod(val days: Int) {
        WEEK(7), MONTH(30), YEAR(365)
    }

    // --- Lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBindings()
        setupLayoutUI()
        setupClickListeners()
        
        setupBarChart()
        setupLineChart()
        setupBottomNavigation()
        loadGraphData()
    }

    override fun onResume() {
        super.onResume()
        loadGraphData()
    }

    // --- Internals

    private fun loadGraphData() = lifecycleScope.launch {
        val userId = authService.getCurrentUser()?.uid ?: return@launch showError("Not logged in")
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

        binds.incomeExpenseLineChart.data = LineData(incomeDataSet, expenseDataSet)
        binds.incomeExpenseLineChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binds.incomeExpenseLineChart.invalidate()

        // Update summary texts
        val totalIncome =
            filteredTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpenses =
            filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val balance = totalIncome - totalExpenses

        binds.totalIncomeText.text = CurrencyFormatter.format(totalIncome)
        binds.totalExpensesText.text = CurrencyFormatter.format(totalExpenses)
        binds.balanceText.text = CurrencyFormatter.format(balance)
    }

    private suspend fun updateCategoryBudgetChart(transactions: List<Transaction>) {
        val filteredTransactions = filterByDate(transactions, currentCategoryTimePeriod.days)
        val categories = fetchCategories(authService.getCurrentUser()?.uid ?: return)

        val entriesActual = ArrayList<BarEntry>()
        val entriesMin = ArrayList<BarEntry>()
        val entriesMax = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        var index = 0f
        for (category in categories) {
            val totalSpent =
                filteredTransactions.filter { it.type == TransactionType.EXPENSE && it.category == category.name }
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

        binds.categoryBudgetBarChart.data = barData
        binds.categoryBudgetBarChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binds.categoryBudgetBarChart.groupBars(0f, 0.4f, 0.05f)
        binds.categoryBudgetBarChart.invalidate()
    }

    private suspend fun fetchTransactions(userId: String): List<Transaction> {
        val accountsSnapshot =
            dataService.collection("users").document(userId).collection("accounts").get().await()
        val transactions = mutableListOf<Transaction>()

        for (doc in accountsSnapshot) {
            val accountId = doc.id
            val txSnapshot =
                dataService.collection("users").document(userId).collection("accounts")
                    .document(accountId)
                    .collection("transactions").get().await()

            transactions.addAll(txSnapshot.toObjects(Transaction::class.java))
        }

        return transactions
    }

    private suspend fun fetchCategories(userId: String): List<Category> {
        val snapshot: QuerySnapshot =
            dataService.collection("users").document(userId).collection("categories").get().await()
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
    
    // --- Event Handlers

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            binds.chipWeek.id -> {
                currentTimePeriod = TimePeriod.WEEK
                loadGraphData()
            }

            binds.chipYear.id -> {
                currentTimePeriod = TimePeriod.YEAR
                loadGraphData()
            }

            binds.chipMonth.id -> {
                currentTimePeriod = TimePeriod.MONTH
                loadGraphData()
            }

            binds.chipCategoryWeek.id -> {
                currentCategoryTimePeriod = TimePeriod.WEEK
                loadGraphData()
            }

            binds.chipCategoryYear.id -> {
                currentCategoryTimePeriod = TimePeriod.YEAR
                loadGraphData()
            }

            binds.chipCategoryMonth.id -> {
                currentCategoryTimePeriod = TimePeriod.MONTH
                loadGraphData()
            }
        }
    }

    private fun setupClickListeners() {
        binds.chipWeek.setOnClickListener(this)
        binds.chipYear.setOnClickListener(this)
        binds.chipMonth.setOnClickListener(this)
        binds.chipCategoryWeek.setOnClickListener(this)
        binds.chipCategoryYear.setOnClickListener(this)
        binds.chipCategoryMonth.setOnClickListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    // --- UI Configuration
    
    private fun setupBarChart() {
        binds.categoryBudgetBarChart.apply {
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
    }
    
    private fun setupLineChart() {
        binds.incomeExpenseLineChart.apply {
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

    private fun setupToolbar() {
        setSupportActionBar(binds.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Insights"
        }
    }

    private fun setupBottomNavigation() {
        binds.bottomNavigation.setOnItemSelectedListener { item ->
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
        binds.bottomNavigation.selectedItemId = R.id.nav_graph
    }

    // --- UI Registrations

    private fun setupBindings() {
        binds = ActivityGraphBinding.inflate(layoutInflater)
    }

    private fun setupLayoutUI() {
        setContentView(binds.root)
        enableEdgeToEdge( // Fix transparent status bar text
            SystemBarStyle.light(
                ContextCompat.getColor(this, R.color.primary),
                ContextCompat.getColor(this, R.color.primary)
            )
        )
        ViewCompat.setOnApplyWindowInsetsListener(binds.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        setupToolbar()
    }
}
