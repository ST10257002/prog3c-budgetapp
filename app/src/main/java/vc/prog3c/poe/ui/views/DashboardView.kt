package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
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
        setupIncomeExpenseGraph()
        observeViewModel()
    }

    private fun setupToolbar() {
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
                    // TODO: Backend Implementation Required
                    // 1. Dashboard Data Refresh:
                    //    - Implement real-time updates
                    //    - Add pull-to-refresh functionality
                    //    - Cache dashboard data locally
                    //    - Handle data synchronization
                    true
                }
                R.id.nav_add_income -> {
                    // TODO: Backend Implementation Required
                    // 1. Income Transaction:
                    //    - Create income record in Firestore
                    //    - Update user balance
                    //    - Add transaction history
                    //    - Implement category tracking
                    startActivity(Intent(this, AddIncomeView::class.java))
                    true
                }
                R.id.nav_add_expense -> {
                    // TODO: Backend Implementation Required
                    // 1. Expense Transaction:
                    //    - Create expense record in Firestore
                    //    - Update user balance
                    //    - Add transaction history
                    //    - Implement category tracking
                    startActivity(Intent(this, AddExpenseView::class.java))
                    true
                }
                R.id.nav_transactions -> {
                    // TODO: Backend Implementation Required
                    // 1. Transaction History:
                    //    - Fetch transactions from Firestore
                    //    - Implement pagination
                    //    - Add filtering and sorting
                    //    - Cache transaction data
                    startActivity(Intent(this, TransactionsView::class.java))
                    true
                }
                R.id.navigation_profile -> {
                    // TODO: Backend Implementation Required
                    // 1. Profile Management:
                    //    - Load user profile data
                    //    - Handle profile updates
                    //    - Manage user preferences
                    //    - Implement data synchronization
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
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

        viewModel.currentSavings.observe(this) { savings ->
            // TODO: Backend Implementation Required
            // 1. Current Savings:
            //    - Calculate current savings from transactions
            //    - Update savings progress
            //    - Handle savings milestones
            //    - Implement savings notifications
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
            binding.currentSavingsText.text = currencyFormat.format(savings)
            binding.savingsProgressBar.progress = viewModel.getSavingsProgress().toInt()
        }
    }
} 