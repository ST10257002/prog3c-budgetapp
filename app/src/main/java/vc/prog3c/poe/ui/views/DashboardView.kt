// DashboardView.kt
package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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
import java.text.NumberFormat
import java.util.Locale

class DashboardView : AppCompatActivity(), View.OnClickListener {

    private lateinit var binds: ActivityDashboardBinding
    private lateinit var model: DashboardViewModel
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Dashboard_Test", "onCreate() called")

        binds = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binds.root)  // <<< Ensure layout is attached first

        model = ViewModelProvider(this)[DashboardViewModel::class.java]

        setupLayoutUi()
        setupClickListeners()
        setupBottomNavigation()
        setupRecyclerView()
        observeViewModel()
    }


    override fun onResume() {
        super.onResume()
        Log.d("Dashboard_Test", "onResume() - refreshing data")
        model.refreshData()
    }

    private fun observeViewModel() = model.uiState.observe(this) { state ->
        Log.d("Dashboard_Test", "observeViewModel() - state: ${state.javaClass.simpleName}")
        when (state) {
            is DashboardUiState.Default -> {
                binds.swipeRefreshLayout.isRefreshing = false
            }
            is DashboardUiState.Loading -> {
                binds.swipeRefreshLayout.isRefreshing = true
            }
            is DashboardUiState.Failure -> {
                binds.swipeRefreshLayout.isRefreshing = false
                Log.e("Dashboard_Test", "Failure: ${state.message}")
                Snackbar.make(binds.root, state.message, Snackbar.LENGTH_LONG).show()
            }
            is DashboardUiState.Updated -> {
                Log.d("Dashboard_Test", "Updated state received")
                state.breakdowns?.let {
                    updateCategoryAdapter(it)
                }
                state.savingsGoals?.let {
                    updateSavingsGoalUI(it)
                    binds.swipeRefreshLayout.isRefreshing = false
                }
                if (state.statistics != null || state.budget != null) {
                    updateBudgetUI(state.budget, state.statistics)
                }
            }
        }
    }

    private fun setupBottomNavigation() {
        Log.d("Dashboard_Test", "Setting up bottom navigation")
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
        Log.d("Dashboard_Test", "Setting up RecyclerView")
        categoryAdapter = CategoryAdapter(
            onEditClick = { },
            onDeleteClick = { })
        binds.categoriesRecyclerView.apply {
            adapter = categoryAdapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@DashboardView)
            setHasFixedSize(true)
        }
    }

    private fun updateSavingsGoalUI(goals: List<SavingsGoal>) {
        try {
            Log.d("Dashboard_Test", "Updating savings goal UI: ${goals.size} goals")
            if (goals.isNotEmpty()) {
                val goal = goals[0]
                val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
                val progress = if (goal.targetAmount > 0) goal.savedAmount / goal.targetAmount else 0.0
                val percent = (progress * 100).toInt().coerceIn(0, 100)

                binds.savingsGoalText.text = "${goal.name}: ${currencyFormat.format(goal.savedAmount)} / ${currencyFormat.format(goal.targetAmount)}"
                binds.currentSavingsText.text = currencyFormat.format(goal.savedAmount)
                binds.maxSavingsText.text = currencyFormat.format(goal.targetAmount)
                binds.savingsPercentageText.text = "$percent%"
                binds.savingsProgressBar.progress = percent

                binds.savingsGoalDate.text = goal.targetDate?.let {
                    val dateFormat = android.text.format.DateFormat.getMediumDateFormat(this)
                    "Target date: ${dateFormat.format(it)}"
                } ?: ""
            } else {
                binds.savingsGoalText.text = getString(R.string.no_savings_goals)
                binds.currentSavingsText.text = "R0"
                binds.maxSavingsText.text = "R0"
                binds.savingsPercentageText.text = "0%"
                binds.savingsProgressBar.progress = 0
                binds.savingsGoalDate.text = ""
            }
        } catch (e: Exception) {
            Log.e("Dashboard_Test", "Exception in updateSavingsGoalUI: ${e.message}", e)
        }
    }


    private fun updateBudgetUI(budget: Budget?, stats: MonthlyStats?) {
       val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
        val spent = stats?.totalExpenses ?: 0.0
        val max = budget?.max ?: 1.0
        val min = budget?.min ?: 0.0

        if (budget != null) {
            binds.budgetAmountText.text = currencyFormat.format(budget.max)
            val monthName = try {
                java.time.Month.of(budget.month).getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault())
            } catch (e: Exception) {
                ""
            }
            binds.budgetMonthText.text = "$monthName ${budget.year}"
        } else {
            binds.budgetAmountText.text = currencyFormat.format(0)
            binds.budgetMonthText.text = ""
        }

        binds.budgetSpentText.text = currencyFormat.format(spent)
        val percent = (spent / max * 100).toInt().coerceIn(0, 100)
        binds.budgetProgressBar.progress = percent
        binds.budgetProgressText.text = "$percent%"

        Log.d("Dashboard_Test", "Budget: min=$min, max=$max, spent=$spent, percent=$percent")

        when {
            spent < min -> binds.budgetSpentText.setTextColor(resources.getColor(R.color.teal_200, theme))
            spent <= max -> binds.budgetSpentText.setTextColor(resources.getColor(R.color.white, theme))
            else -> binds.budgetSpentText.setTextColor(resources.getColor(R.color.red, theme))
        }
    }

    private fun updateCategoryAdapter(breakdown: Map<String, Double>?) {
        val breakdownSafe = breakdown ?: emptyMap()
        Log.d("Dashboard_Test", "updateCategoryAdapter() with breakdown: $breakdownSafe")

        if (breakdownSafe.isEmpty()) {
            binds.noCategoriesText.visibility = View.VISIBLE
            binds.categoriesRecyclerView.visibility = View.GONE
        } else {
            binds.noCategoriesText.visibility = View.GONE
            binds.categoriesRecyclerView.visibility = View.VISIBLE

            val existingCategories = model.categories.value ?: emptyList()
            val distinctCategories = existingCategories.distinctBy { it.name }
            Log.d("Dashboard_Test", "Loaded distinct categories: ${distinctCategories.size}")

            val sortedCategories = distinctCategories.sortedWith(
                compareBy<Category> { it.type != CategoryType.SAVINGS }
                    .thenBy { it.type != CategoryType.EMERGENCY }
                    .thenBy { it.type != CategoryType.UTILITIES }
                    .thenBy { it.name }
            )

            Log.d("Dashboard_Test", "Sorted categories: ${sortedCategories.map { it.name }}")

            categoryAdapter.submitList(sortedCategories)
            categoryAdapter.updateCategoryTotals(breakdownSafe)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        Log.d("Dashboard_Test", "onSupportNavigateUp() called")
        onBackPressed()
        return true
    }

    override fun onClick(view: View?) {
        Log.d("Dashboard_Test", "onClick() - viewId: ${view?.id}")
        when (view?.id) {
            binds.profileImage.id -> startActivity(Intent(this, ProfileActivity::class.java))
            binds.manageCategoriesButton.id -> startActivity(Intent(this, CategoryManagementActivity::class.java))
            binds.manageGoalsButton.id -> startActivity(Intent(this, ManageGoalsActivity::class.java))
        }
    }

    private fun setupClickListeners() {
        Log.d("Dashboard_Test", "Setting up click listeners")
        binds.profileImage.setOnClickListener(this)
        binds.manageCategoriesButton.setOnClickListener(this)
        binds.manageGoalsButton.setOnClickListener(this)
    }

    private fun setupToolbar() {
        Log.d("Dashboard_Test", "Setting up toolbar")
        setSupportActionBar(binds.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.title = "Dashboard"
    }

    private fun setupSwipeRefresh() {
        Log.d("Dashboard_Test", "Setting up swipe refresh")
        binds.swipeRefreshLayout.apply {
            setColorSchemeResources(R.color.primary, R.color.green, R.color.red)
            setOnRefreshListener {
                Log.d("Dashboard_Test", "Swipe refresh triggered")
                model.refreshData()
            }
        }
    }

    private fun setupBindings() {
        Log.d("Dashboard_Test", "Binding layout")
        binds = ActivityDashboardBinding.inflate(layoutInflater)
    }

    private fun setupLayoutUi() {
        Log.d("Dashboard_Test", "Setting up layout UI")
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