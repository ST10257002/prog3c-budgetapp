// DashboardView.kt
package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import vc.prog3c.poe.R
import vc.prog3c.poe.data.models.Budget
import vc.prog3c.poe.data.models.Category
import vc.prog3c.poe.data.models.CategoryType
import vc.prog3c.poe.data.models.MonthlyStats
import vc.prog3c.poe.data.models.SavingsGoal
import vc.prog3c.poe.databinding.ActivityDashboardBinding
import vc.prog3c.poe.ui.adapters.CategoryAdapter
import vc.prog3c.poe.ui.viewmodels.DashboardUiState
import vc.prog3c.poe.ui.viewmodels.DashboardViewModel
import vc.prog3c.poe.utils.CurrencyFormatter
import java.text.NumberFormat
import java.util.Locale

class DashboardView : AppCompatActivity(), View.OnClickListener {

    private lateinit var binds: ActivityDashboardBinding
    private lateinit var model: DashboardViewModel
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBindings()
        setupLayoutUi()
        setupClickListeners()

        model = ViewModelProvider(this)[DashboardViewModel::class.java]

        setupBottomNavigation()
        setupRecyclerView()

        observeViewModel()
        model.refreshData()
    }

    override fun onResume() {
        super.onResume()
        model.refreshData()
    }

    private fun observeViewModel() = model.uiState.observe(this) { state ->
        when (state) {
            is DashboardUiState.Default -> {
                binds.swipeRefreshLayout.isRefreshing = false
            }

            is DashboardUiState.Loading -> {
                binds.swipeRefreshLayout.isRefreshing = true
            }

            is DashboardUiState.Failure -> {
                binds.swipeRefreshLayout.isRefreshing = false
                Snackbar.make(binds.root, state.message, Snackbar.LENGTH_LONG).show()
            }

            is DashboardUiState.Updated -> {
                updateCategoryAdapter(state.categoryList ?: emptyList(), state.breakdowns ?: emptyMap())

                state.savingsGoals?.let {
                    updateSavingsGoalUI(it)
                }

                if (state.statistics != null || state.budget != null) {
                    updateBudgetUI(state.budget, state.statistics)
                }

                binds.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun setupBottomNavigation() {
        binds.bottomNavigation.setOnItemSelectedListener { item ->
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
        binds.bottomNavigation.selectedItemId = R.id.nav_dashboard
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(
            onEditClick = { category ->
                // TODO: Implement category editing
                Toast.makeText(this, "Edit category: ${category.name}", Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { category ->
                // TODO: Implement category deletion
                Toast.makeText(this, "Delete category: ${category.name}", Toast.LENGTH_SHORT).show()
            }
        )
        binds.categoriesRecyclerView.apply {
            adapter = categoryAdapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@DashboardView)
            setHasFixedSize(true)
        }
    }

    private fun updateSavingsGoalUI(goals: List<SavingsGoal>) {
        if (goals.isNotEmpty()) {
            val goal = goals[0]
            val progress = if (goal.targetAmount > 0) goal.savedAmount / goal.targetAmount else 0.0
            val percent = (progress * 100).toInt().coerceIn(0, 100)

            binds.savingsGoalText.text = "${goal.name}: ${CurrencyFormatter.format(goal.savedAmount)} / ${CurrencyFormatter.format(goal.targetAmount)}"
            binds.currentSavingsText.text = CurrencyFormatter.format(goal.savedAmount)
            binds.maxSavingsText.text = CurrencyFormatter.format(goal.targetAmount)
            binds.savingsPercentageText.text = "$percent%"
            binds.savingsProgressBar.progress = percent

            binds.savingsGoalDate.text = goal.targetDate?.let {
                val dateFormat = android.text.format.DateFormat.getMediumDateFormat(this)
                "Target date: ${dateFormat.format(it)}"
            } ?: ""
        } else {
            binds.savingsGoalText.text = getString(R.string.no_savings_goals)
            binds.currentSavingsText.text = CurrencyFormatter.format(0)
            binds.maxSavingsText.text = CurrencyFormatter.format(0)
            binds.savingsPercentageText.text = "0%"
            binds.savingsProgressBar.progress = 0
            binds.savingsGoalDate.text = ""
        }
    }

    private fun updateBudgetUI(budget: Budget?, stats: MonthlyStats?) {
        val spent = stats?.totalExpenses ?: 0.0
        val max = budget?.max ?: 1.0
        val min = budget?.min ?: 0.0

        binds.budgetAmountText.text = CurrencyFormatter.format(budget?.max ?: 0.0)
        binds.budgetMonthText.text = budget?.let {
            try {
                val monthName = java.time.Month.of(it.month)
                    .getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault())
                "$monthName ${it.year}"
            } catch (e: Exception) {
                ""
            }
        } ?: ""

        binds.budgetSpentText.text = CurrencyFormatter.format(spent)
        val percent = (spent / max * 100).toInt().coerceIn(0, 100)
        binds.budgetProgressBar.progress = percent
        binds.budgetProgressText.text = "$percent%"

        val spentColor = when {
            spent < min -> R.color.teal_200
            spent <= max -> R.color.white
            else -> R.color.red
        }
        binds.budgetSpentText.setTextColor(resources.getColor(spentColor, theme))
    }

    private fun updateCategoryAdapter(
        categories: List<Category>,
        breakdown: Map<String, Double>
    ) {
        if (breakdown.isEmpty()) {
            binds.noCategoriesText.visibility = View.VISIBLE
            binds.categoriesRecyclerView.visibility = View.GONE
            return
        }

        binds.noCategoriesText.visibility = View.GONE
        binds.categoriesRecyclerView.visibility = View.VISIBLE

        val allCategories = categories.toMutableList()

        if (allCategories.none { it.type == CategoryType.SAVINGS }) {
            allCategories.add(Category("savings", "Savings", CategoryType.SAVINGS, "ic_savings", "#4CAF50", false))
        }
        if (allCategories.none { it.type == CategoryType.EMERGENCY }) {
            allCategories.add(Category("emergency", "Emergency Fund", CategoryType.EMERGENCY, "ic_error", "#F44336", false))
        }
        if (allCategories.none { it.type == CategoryType.UTILITIES }) {
            allCategories.add(Category("utilities", "Utilities", CategoryType.UTILITIES, "ic_utilities", "#2196F3", false))
        }

        val sortedCategories = allCategories.sortedWith(
            compareBy<Category> { it.type != CategoryType.SAVINGS }
                .thenBy { it.type != CategoryType.EMERGENCY }
                .thenBy { it.type != CategoryType.UTILITIES }
                .thenBy { it.name }
        )

        categoryAdapter.submitList(sortedCategories)
        categoryAdapter.updateCategoryTotals(breakdown)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            binds.profileImage.id -> startActivity(Intent(this, ProfileActivity::class.java))
            binds.manageCategoriesButton.id -> startActivity(Intent(this, CategoryManagementActivity::class.java))
            binds.manageGoalsButton.id -> startActivity(Intent(this, ManageGoalsActivity::class.java))
        }
    }

    private fun setupClickListeners() {
        binds.profileImage.setOnClickListener(this)
        binds.manageCategoriesButton.setOnClickListener(this)
        binds.manageGoalsButton.setOnClickListener(this)
    }

    private fun setupToolbar() {
        setSupportActionBar(binds.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.title = "Dashboard"
    }

    private fun setupSwipeRefresh() {
        binds.swipeRefreshLayout.apply {
            setColorSchemeResources(R.color.primary, R.color.green, R.color.red)
            setOnRefreshListener { model.refreshData() }
        }
    }

    private fun setupBindings() {
        binds = ActivityDashboardBinding.inflate(layoutInflater)
    }

    private fun setupLayoutUi() {
        setContentView(binds.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binds.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupToolbar()
        setupSwipeRefresh()
    }
}