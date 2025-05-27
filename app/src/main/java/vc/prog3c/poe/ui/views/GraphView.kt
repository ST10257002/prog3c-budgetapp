package vc.prog3c.poe.ui.views

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.snackbar.Snackbar
import vc.prog3c.poe.R
import vc.prog3c.poe.databinding.ActivityGraphViewBinding
import vc.prog3c.poe.ui.viewmodels.GraphViewModel // Correct import
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class GraphView : AppCompatActivity() {
    private lateinit var binding: ActivityGraphViewBinding
    private lateinit var viewModel: GraphViewModel // Correct reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGraphViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[GraphViewModel::class.java] // Correct reference

        setupToolbar()
        setupBottomNavigation()
        setupSwipeRefresh()
        setupChart() // Setup the chart initially
        observeViewModel()

        // Initial data load
        viewModel.loadGraphData() // Call ViewModel function
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Graphs"
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, DashboardView::class.java))
                    finish()
                    true
                }
                R.id.nav_accounts -> {
                    startActivity(Intent(this, AccountsView::class.java))
                    finish()
                    true
                }
                R.id.nav_graph -> {
                    true // Stay on this screen
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
        binding.bottomNavigation.selectedItemId = R.id.nav_graph
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.apply {
            setColorSchemeResources(
                R.color.primary,
                R.color.green,
                R.color.red
            )
            setOnRefreshListener {
                refreshData()
            }
        }
    }

    private fun refreshData() {
        viewModel.refreshGraphData() // Call ViewModel function
    }


    private fun setupChart() {
        binding.lineChart.apply { // Use binding.lineChart
            description.isEnabled = false
            setTouchEnabled(true)
            setDrawGridBackground(false)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)

            // Configure X-axis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.setDrawAxisLine(true)
            xAxis.textColor = Color.BLACK
            xAxis.textSize = 10f
            xAxis.setAvoidFirstLastClipping(true)

            // Configure Y-axis (left)
            axisLeft.setDrawGridLines(true)
            axisLeft.setDrawAxisLine(true)
            axisLeft.textColor = Color.BLACK
            axisLeft.textSize = 10f

            // Configure Y-axis (right) - disable or customize if needed
            axisRight.isEnabled = false // Disable right Y-axis

            legend.isEnabled = true // Enable legend if you have multiple datasets (e.g., Income vs Expense lines)
            animateX(1000)

            // Improve appearance
            extraBottomOffset = 10f // Add some offset to prevent labels from being cut off
            setExtraOffsets(10f, 10f, 10f, 10f) // Add extra space around the chart
        }
    }

    private fun observeViewModel() {
        viewModel.incomeExpenseData.observe(this) { data ->
            // Update the chart with the observed data
            updateChart(data)
        }

        viewModel.totalIncome.observe(this) { income ->
            // Update UI elements for total income if they exist
            // binding.totalIncomeTextView.text = formatCurrency(income) // Example
        }

        viewModel.totalExpenses.observe(this) { expenses ->
            // Update UI elements for total expenses if they exist
            // binding.totalExpensesTextView.text = formatCurrency(expenses) // Example
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
            binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE // Use binding.loadingProgressBar
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                showError(it)
            }
        }
    }

    private fun updateChart(data: Map<Long, Pair<Double, Double>>) {
        if (data.isEmpty()) {
            binding.lineChart.data = null // Use binding.lineChart
            binding.lineChart.invalidate() // Use binding.lineChart
            return
        }

        // Example: Creating separate datasets for Income and Expense
        val incomeEntries = ArrayList<Entry>()
        val expenseEntries = ArrayList<Entry>()

        data.forEach { (dateMillis, totals) ->
            incomeEntries.add(Entry(dateMillis.toFloat(), totals.first.toFloat()))
            expenseEntries.add(Entry(dateMillis.toFloat(), totals.second.toFloat()))
        }

        val incomeDataSet = LineDataSet(incomeEntries, "Income").apply {
            color = resources.getColor(R.color.green, null) // Define green color resource
            valueTextColor = Color.BLACK
            setCircleColor(resources.getColor(R.color.green, null))
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            lineWidth = 2f
        }

        val expenseDataSet = LineDataSet(expenseEntries, "Expense").apply {
            color = resources.getColor(R.color.red, null) // Define red color resource
            valueTextColor = Color.BLACK
            setCircleColor(resources.getColor(R.color.red, null))
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            lineWidth = 2f
        }


        val lineData = LineData(incomeDataSet, expenseDataSet) // Add both datasets

        binding.lineChart.data = lineData // Use binding.lineChart

        // Format X-axis to show dates
        val xAxis = binding.lineChart.xAxis // Use binding.lineChart.xAxis
        xAxis.valueFormatter = object : IndexAxisValueFormatter() {
            private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            override fun getFormattedValue(value: Float): String {
                return dateFormat.format(Date(value.toLong()))
            }
        }
        // Adjust label count and granularity based on your data range
        xAxis.setLabelCount(data.size, true)
        xAxis.granularity = TimeUnit.DAYS.toMillis(1).toFloat() // Example: labels every day
        xAxis.labelRotationAngle = -45f

        binding.lineChart.invalidate() // Use binding.lineChart
        binding.lineChart.animateX(1000) // Use binding.lineChart
    }


    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show() // Use binding.root
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}