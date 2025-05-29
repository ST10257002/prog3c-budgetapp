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
import vc.prog3c.poe.data.models.Category
import vc.prog3c.poe.data.models.CategoryType
import vc.prog3c.poe.databinding.ActivityDashboardBinding
import vc.prog3c.poe.ui.adapters.CategoryAdapter
import vc.prog3c.poe.ui.viewmodels.DashboardViewModel
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

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
        setupRecyclerView()
        setupViews()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        refreshData()
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

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(
            onEditClick = { /* Handle edit if needed */ },
            onDeleteClick = { /* Handle delete if needed */ }
        )
        binding.categoriesRecyclerView.apply {
            adapter = categoryAdapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@DashboardView)
            setHasFixedSize(true)
        }
    }

    private fun setupViews() {
        binding.manageGoalsButton.setOnClickListener {
            val intent = Intent(this, ManageGoalsActivity::class.java)
            startActivity(intent)
        }

        binding.manageCategoriesButton.setOnClickListener {
            val intent = Intent(this, CategoryManagementActivity::class.java)
            startActivity(intent)
        }
    }

    private fun refreshData() {
        viewModel.refreshData()
        binding.swipeRefreshLayout.isRefreshing = true
    }

    private fun observeViewModel() {
        viewModel.savingsGoals.observe(this) { goals ->
            updateSavingsGoalUI(goals)
            binding.swipeRefreshLayout.isRefreshing = false
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
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
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
            
            // Get all categories from the ViewModel and ensure preset categories are included
            val existingCategories = viewModel.categories.value ?: emptyList()
            val allCategories = existingCategories.toMutableList()
            
            // Add preset categories if they don't exist
            if (existingCategories.none { category -> category.type == CategoryType.SAVINGS }) {
                allCategories.add(Category(
                    id = "savings",
                    name = "Savings",
                    type = CategoryType.SAVINGS,
                    icon = "ic_savings",
                    color = "#4CAF50",
                    isEditable = false
                ))
            }
            if (existingCategories.none { category -> category.type == CategoryType.EMERGENCY }) {
                allCategories.add(Category(
                    id = "emergency",
                    name = "Emergency Fund",
                    type = CategoryType.EMERGENCY,
                    icon = "ic_error",
                    color = "#F44336",
                    isEditable = false
                ))
            }
            if (existingCategories.none { category -> category.type == CategoryType.UTILITIES }) {
                allCategories.add(Category(
                    id = "utilities",
                    name = "Utilities",
                    type = CategoryType.UTILITIES,
                    icon = "ic_utilities",
                    color = "#2196F3",
                    isEditable = false
                ))
            }

            // Sort categories: preset categories first, then custom categories
            val sortedCategories = allCategories.sortedWith(
                compareBy<Category> { it.type != CategoryType.SAVINGS }
                    .thenBy { it.type != CategoryType.EMERGENCY }
                    .thenBy { it.type != CategoryType.UTILITIES }
                    .thenBy { it.name }
            )

            categoryAdapter.submitList(sortedCategories)
            categoryAdapter.updateCategoryTotals(breakdownSafe)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
