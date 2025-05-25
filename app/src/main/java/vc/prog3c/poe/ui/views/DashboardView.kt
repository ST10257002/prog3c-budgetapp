package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import vc.prog3c.poe.R
import vc.prog3c.poe.databinding.ActivityDashboardBinding
import vc.prog3c.poe.ui.viewmodels.DashboardViewModel
import java.text.NumberFormat
import java.util.Locale

class DashboardView : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var viewModel: DashboardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        setupToolbar()
        setupBottomNavigation()
        setupSwipeRefresh()
        setupIncomeExpenseGraph()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.title = "Dashboard"

        binding.profileImage.setOnClickListener {
            // TODO: Backend Implementation Required
            // 1. User Profile Data:
            //    - Fetch user profile from Firestore
            //    - Load profile image from Firebase Storage
            //    - Update last active timestamp
            //    - Handle offline state
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    true
                }
                R.id.nav_transactions -> {
                    startActivity(Intent(this, TransactionsView::class.java))
                    true
                }
                R.id.nav_add_income -> {
                    startActivity(Intent(this, AddIncomeView::class.java))
                    true
                }
                R.id.nav_add_expense -> {
                    startActivity(Intent(this, AddExpenseView::class.java))
                    true
                }
                else -> false
            }
        }
        binding.bottomNavigation.selectedItemId = R.id.nav_dashboard
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
        // TODO: Backend Implementation Required
        // 1. Dashboard Data Refresh:
        //    - Implement real-time updates
        //    - Add pull-to-refresh functionality
        //    - Cache dashboard data locally
        //    - Handle data synchronization
        viewModel.refreshDashboardData()
    }

    private fun setupIncomeExpenseGraph() {
        binding.pieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            setEntryLabelTextSize(12f)
            setEntryLabelColor(android.graphics.Color.BLACK)
            legend.textSize = 12f
            legend.textColor = android.graphics.Color.BLACK
            setDrawEntryLabels(true)
            isDrawHoleEnabled = true
            setHoleColor(android.graphics.Color.WHITE)
            transparentCircleRadius = 30f
            holeRadius = 30f
            setRotationAngle(0f)
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            animateY(1000)
        }
    }

    private fun observeViewModel() {
        viewModel.incomeExpenseData.observe(this) { data ->
            // TODO: Backend Implementation Required
            // 1. Financial Data:
            //    - Fetch income/expense data from Firestore
            //    - Calculate totals and percentages
            //    - Update charts in real-time
            //    - Cache financial data locally
            binding.pieChart.data = data.pieData
            binding.pieChart.invalidate()
            binding.pieChart.animateY(1000)

            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
            binding.totalIncomeText.text = currencyFormat.format(data.totalIncome)
            binding.totalExpensesText.text = currencyFormat.format(data.totalExpenses)
            binding.balanceText.text = currencyFormat.format(data.totalIncome - data.totalExpenses)
        }

        viewModel.savingsGoal.observe(this) { goal ->
            // TODO: Backend Implementation Required
            // 1. Savings Goals:
            //    - Fetch savings goal from Firestore
            //    - Calculate progress
            //    - Update goal status
            //    - Handle goal completion
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
            binding.savingsGoalText.text = currencyFormat.format(goal)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
            binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                showError(it)
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Retry") {
                refreshData()
            }
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 