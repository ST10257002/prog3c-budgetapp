package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
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


    // --- Lifecycle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBindings()
        setupLayoutUi()
        setupClickListeners()

        model = ViewModelProvider(this)[DashboardViewModel::class.java]

        setupBottomNavigation()
        setupRecyclerView()

        observeViewModel()
    }


    override fun onResume() {
        super.onResume()
        model.refreshData()
    }


    // --- ViewModel


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
                Snackbar.make(
                    binds.root, state.message, Snackbar.LENGTH_LONG
                ).show()
            }

            is DashboardUiState.Updated -> {
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


    // --- Internals


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
            onEditClick = { /* Handle edit if needed */ },
            onDeleteClick = { /* Handle delete if needed */ })
        binds.categoriesRecyclerView.apply {
            adapter = categoryAdapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@DashboardView)
            setHasFixedSize(true)
        }
    }


    private fun updateSavingsGoalUI(goals: List<SavingsGoal>) {
        if (goals.isNotEmpty()) {
            val goal = goals[0]
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
            val progress = if (goal.targetAmount > 0) goal.savedAmount / goal.targetAmount else 0.0
            val percent = (progress * 100).toInt().coerceIn(0, 100)

            binds.savingsGoalText.text =
                "${goal.name}: ${currencyFormat.format(goal.savedAmount)} / ${
                    currencyFormat.format(goal.targetAmount)
                }"
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
    }


    private fun updateBudgetUI(budget: Budget?, stats: MonthlyStats?) {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
        val spent = stats?.totalExpenses ?: 0.0
        val max = budget?.max ?: 1.0
        val min = budget?.min ?: 0.0

        if (budget != null) {
            binds.budgetAmountText.text = currencyFormat.format(budget.max)
            val monthName = try {
                java.time.Month.of(budget.month)
                    .getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault())
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

        when {
            spent < min -> binds.budgetSpentText.setTextColor(
                resources.getColor(
                    R.color.teal_200, theme
                )
            )

            spent <= max -> binds.budgetSpentText.setTextColor(
                resources.getColor(
                    R.color.white, theme
                )
            )

            else -> binds.budgetSpentText.setTextColor(resources.getColor(R.color.red, theme))
        }
    }


    private fun updateCategoryAdapter(breakdown: Map<String, Double>?) {
        val breakdownSafe = breakdown ?: emptyMap()

        if (breakdownSafe.isEmpty()) {
            binds.noCategoriesText.visibility = View.VISIBLE
            binds.categoriesRecyclerView.visibility = View.GONE
        } else {
            binds.noCategoriesText.visibility = View.GONE
            binds.categoriesRecyclerView.visibility = View.VISIBLE

            // Get all categories from the ViewModel and ensure preset categories are included
            val existingCategories = model.categories.value ?: emptyList()
            val allCategories = existingCategories.toMutableList()

            // Add preset categories if they don't exist
            if (existingCategories.none { category -> category.type == CategoryType.SAVINGS }) {
                allCategories.add(
                    Category(
                        id = "savings",
                        name = "Savings",
                        type = CategoryType.SAVINGS,
                        icon = "ic_savings",
                        color = "#4CAF50",
                        isEditable = false
                    )
                )
            }
            if (existingCategories.none { category -> category.type == CategoryType.EMERGENCY }) {
                allCategories.add(
                    Category(
                        id = "emergency",
                        name = "Emergency Fund",
                        type = CategoryType.EMERGENCY,
                        icon = "ic_error",
                        color = "#F44336",
                        isEditable = false
                    )
                )
            }
            if (existingCategories.none { category -> category.type == CategoryType.UTILITIES }) {
                allCategories.add(
                    Category(
                        id = "utilities",
                        name = "Utilities",
                        type = CategoryType.UTILITIES,
                        icon = "ic_utilities",
                        color = "#2196F3",
                        isEditable = false
                    )
                )
            }

            // Sort categories: preset categories first, then custom categories
            val sortedCategories =
                allCategories.sortedWith(compareBy<Category> { it.type != CategoryType.SAVINGS }.thenBy { it.type != CategoryType.EMERGENCY }
                    .thenBy { it.type != CategoryType.UTILITIES }.thenBy { it.name })

            categoryAdapter.submitList(sortedCategories)
            categoryAdapter.updateCategoryTotals(breakdownSafe)
        }
    }


    // --- Event Handlers


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            binds.profileImage.id -> startActivity(Intent(this, ProfileActivity::class.java))
            binds.manageCategoriesButton.id -> {
                val intent = Intent(this, CategoryManagementActivity::class.java)
                startActivity(intent)
            }

            binds.manageGoalsButton.id -> {
                val intent = Intent(this, ManageGoalsActivity::class.java)
                startActivity(intent)
            }
        }
    }


    private fun setupClickListeners() {
        binds.profileImage.setOnClickListener(this)
        binds.manageCategoriesButton.setOnClickListener(this)
        binds.manageGoalsButton.setOnClickListener(this)
    }


    // --- UI Configuration


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


    // --- UI Registrations


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
