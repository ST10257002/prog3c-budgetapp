package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import vc.prog3c.poe.R
import vc.prog3c.poe.data.models.Budget
import vc.prog3c.poe.data.models.MonthlyStats
import vc.prog3c.poe.data.models.SavingsGoal
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

        categoryAdapter = CategoryAdapter(emptyMap())
        binding.categoriesRecyclerView.adapter = categoryAdapter

        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        setupToolbar()
        setupBottomNavigation()
        setupSwipeRefresh()
        observeViewModel()

        binding.manageGoalsView.initialize(viewModel, this)
        binding.editGoalButton.setOnClickListener {
            binding.manageGoalsView.visibility = if (binding.manageGoalsView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        viewModel.refreshData()
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
                R.id.nav_dashboard -> true
                R.id.nav_accounts -> {
                    startActivity(Intent(this, AccountsView::class.java))
                    true
                }
                R.id.nav_graph -> {
                    startActivity(Intent(this, GraphView::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
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

    private fun refreshData() {
        viewModel.refreshData()
    }

    private fun observeViewModel() {
        viewModel.savingsGoals.observe(this) { goals ->
            updateSavingsGoalUI(goals)
        }

        viewModel.budget.observe(this) { budget ->
            updateBudgetUI(budget, viewModel.monthlyStats.value)
        }
        viewModel.monthlyStats.observe(this) { stats ->
            updateBudgetUI(viewModel.budget.value, stats)
        }

        viewModel.categoryBreakdown.observe(this) { breakdown ->
            updateCategoryAdapter(breakdown)
        }

        viewModel.error.observe(this) { error ->
            if (error == null && binding.manageGoalsView.visibility == View.VISIBLE) {
                binding.manageGoalsView.visibility = View.GONE
                Snackbar.make(binding.root, "Goal updated!", Snackbar.LENGTH_SHORT).show()
            } else if (error != null) {
                Snackbar.make(binding.root, error, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateSavingsGoalUI(goals: List<SavingsGoal>) {
        if (goals.isNotEmpty()) {
            val goal = goals[0]
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
            val progress = if (goal.targetAmount > 0) goal.savedAmount / goal.targetAmount else 0.0
            val percent = (progress * 100).toInt().coerceIn(0, 100)

            binding.savingsGoalText.text = "${goal.name}: ${currencyFormat.format(goal.savedAmount)} / ${currencyFormat.format(goal.targetAmount)}"
            binding.currentSavingsText.text = currencyFormat.format(goal.savedAmount)
            binding.maxSavingsText.text = currencyFormat.format(goal.targetAmount)
            binding.savingsPercentageText.text = "$percent%"
            binding.savingsProgressBar.progress = percent

            binding.savingsGoalDate.text = goal.targetDate?.let {
                val dateFormat = android.text.format.DateFormat.getMediumDateFormat(this)
                "Target date: ${dateFormat.format(it)}"
            } ?: ""
        } else {
            binding.savingsGoalText.text = getString(R.string.no_savings_goals)
            binding.currentSavingsText.text = "R0"
            binding.maxSavingsText.text = "R0"
            binding.savingsPercentageText.text = "0%"
            binding.savingsProgressBar.progress = 0
            binding.savingsGoalDate.text = ""
        }
    }

    private fun updateBudgetUI(budget: Budget?, stats: MonthlyStats?) {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
        val spent = stats?.totalExpenses ?: 0.0
        val max = budget?.max ?: 1.0
        val min = budget?.min ?: 0.0

        if (budget != null) {
            binding.budgetAmountText.text = currencyFormat.format(budget.max)
            val monthName = try {
                java.time.Month.of(budget.month).getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault())
            } catch (e: Exception) { "" }
            binding.budgetMonthText.text = "$monthName ${budget.year}"
        } else {
            binding.budgetAmountText.text = currencyFormat.format(0)
            binding.budgetMonthText.text = ""
        }

        binding.budgetSpentText.text = currencyFormat.format(spent)
        val percent = (spent / max * 100).toInt().coerceIn(0, 100)
        binding.budgetProgressBar.progress = percent
        binding.budgetProgressText.text = "$percent%"

        when {
            spent < min -> binding.budgetSpentText.setTextColor(resources.getColor(R.color.teal_200, theme))
            spent <= max -> binding.budgetSpentText.setTextColor(resources.getColor(R.color.white, theme))
            else -> binding.budgetSpentText.setTextColor(resources.getColor(R.color.red, theme))
        }
    }

    private fun updateCategoryAdapter(breakdown: Map<String, Double>?) {
        val breakdownSafe = breakdown ?: emptyMap()

        if (breakdownSafe.isEmpty()) {
            binding.noCategoriesText.visibility = View.VISIBLE
            binding.categoriesRecyclerView.visibility = View.GONE
        } else {
            binding.noCategoriesText.visibility = View.GONE
            binding.categoriesRecyclerView.visibility = View.VISIBLE
            categoryAdapter.updateCategoryData(breakdownSafe)

            // 🔥 Force re-binding in case of stale adapter
            binding.categoriesRecyclerView.adapter = categoryAdapter
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
}
