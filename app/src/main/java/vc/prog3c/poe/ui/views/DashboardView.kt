package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import vc.prog3c.poe.R
import vc.prog3c.poe.databinding.ActivityDashboardBinding
import vc.prog3c.poe.ui.adapters.CategoryAdapter
import vc.prog3c.poe.ui.viewmodels.DashboardViewModel
import java.text.NumberFormat
import java.util.Locale

class DashboardView : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var viewModel: DashboardViewModel
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        setupToolbar()
        setupBottomNavigation()
        setupSwipeRefresh()
        setupCategoriesRecyclerView()
        observeViewModel()

        viewModel.refreshData() // trigger initial data load
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.title = "Dashboard"

        binding.profileImage.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    // Already on dashboard
                    true
                }
                R.id.nav_accounts -> {
                    startActivity(Intent(this, AccountsView::class.java))
                    finish() // Close current activity
                    true
                }
                R.id.nav_graph -> {
                    startActivity(Intent(this, GraphView::class.java))
                    finish() // Close current activity
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish() // Close current activity
                    true
                }
                else -> false
            }
        }
        binding.bottomNavigation.selectedItemId = R.id.nav_dashboard
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.apply {
            setColorSchemeResources(R.color.primary, R.color.green, R.color.red)
            setOnRefreshListener { refreshData() }
        }
    }

    private fun setupCategoriesRecyclerView() {
        categoryAdapter = CategoryAdapter(emptyList())
        binding.categoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DashboardView)
            adapter = categoryAdapter
        }
    }

    private fun refreshData() {
        viewModel.refreshData()
    }

    private fun observeViewModel() {
        // Observe savings goals
        viewModel.savingsGoals.observe(this) { goals ->
            if (goals.isNotEmpty()) {
                val goal = goals[0]
                val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
                binding.savingsGoalText.text = "Goal: ${currencyFormat.format(goal.targetAmount)}"
                binding.currentSavingsText.text = currencyFormat.format(goal.savedAmount)
                binding.maxSavingsText.text = currencyFormat.format(goal.targetAmount)
                binding.savingsPercentageText.text = "${viewModel.getSavingsProgress(goal).toInt()}%"
                
                // Update progress bar
                binding.savingsProgressBar.progress = viewModel.getSavingsProgress(goal).toInt()
            }
        }

        // Observe monthly stats
        viewModel.monthlyStats.observe(this) { stats ->
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
            // Removed Income vs Expenses text view updates
            // binding.totalIncomeText.text = currencyFormat.format(stats.totalIncome)
            // binding.totalExpensesText.text = currencyFormat.format(stats.totalExpenses)
            // binding.savingsText.text = "Savings: ${currencyFormat.format(stats.savings)}"
        }

        // Observe current budget
        viewModel.currentBudget.observe(this) { budget ->
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
            binding.budgetMonthText.text = budget.month
            binding.budgetAmountText.text = currencyFormat.format(budget.amount)
            binding.budgetSpentText.text = currencyFormat.format(budget.spent)
            
            // Calculate and display budget progress
            val progress = ((budget.spent / budget.amount) * 100).toInt()
            binding.budgetProgressBar.progress = progress
            binding.budgetProgressText.text = "$progress%"
        }

        // Observe categories
        viewModel.categories.observe(this) { categories ->
            if (categories.isEmpty()) {
                binding.noCategoriesText.visibility = View.VISIBLE
                binding.categoriesRecyclerView.visibility = View.GONE
            } else {
                binding.noCategoriesText.visibility = View.GONE
                binding.categoriesRecyclerView.visibility = View.VISIBLE
                categoryAdapter.updateCategories(categories, viewModel.getCategoryBreakdown())
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
            binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe errors
        viewModel.error.observe(this) { error ->
            error?.let { showError(it) }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Retry") { refreshData() }
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onResume() {
        super.onResume()
        // Ensure dashboard is selected when returning to this activity
        binding.bottomNavigation.selectedItemId = R.id.nav_dashboard
    }
}