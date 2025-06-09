// DashboardView.kt
package vc.prog3c.poe.ui.views
/**
 * @reference MPAndroidChart - PieChart: https://github.com/PhilJay/MPAndroidChart/wiki/PieChart
 * @reference ConstraintLayout: https://developer.android.com/reference/androidx/constraintlayout/widget/ConstraintLayout
 * @reference ViewModelScope + LiveData Observation: https://developer.android.com/topic/libraries/architecture/livedata
 */

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
import vc.prog3c.poe.core.utils.CurrencyFormatter
import vc.prog3c.poe.data.models.Achievement
import vc.prog3c.poe.ui.viewmodels.AchievementViewModel
import java.text.NumberFormat
import java.util.Locale
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.switchmaterial.SwitchMaterial
import android.widget.AutoCompleteTextView
import android.app.AlertDialog
import android.widget.ArrayAdapter

class DashboardView : AppCompatActivity(), View.OnClickListener {

    private lateinit var binds: ActivityDashboardBinding
    private lateinit var model: DashboardViewModel
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var achievementViewModel: AchievementViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBindings()
        setupLayoutUi()
        setupClickListeners()
        setupSavingsGoalCard()

        model = ViewModelProvider(this)[DashboardViewModel::class.java]

        setupBottomNavigation()
        setupRecyclerView()

        observeViewModel()
        model.refreshData()

        achievementViewModel = ViewModelProvider(this)[AchievementViewModel::class.java]

        achievementViewModel.newlyCompleted.observe(this) { event ->
            event.getContentIfNotHandled()?.let { achievement ->
                showAchievementSnackbar(achievement)
            }
        }

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
                if (!category.isEditable) {
                    Snackbar.make(binds.root, "This category cannot be edited", Snackbar.LENGTH_SHORT).show()
                    return@CategoryAdapter
                }
                startActivity(Intent(this, CategoryManagementActivity::class.java).apply {
                    putExtra("categoryId", category.id)
                })
            },
            onDeleteClick = { category ->
                if (!category.isEditable) {
                    Snackbar.make(binds.root, "This category cannot be deleted", Snackbar.LENGTH_SHORT).show()
                    return@CategoryAdapter
                }
                MaterialAlertDialogBuilder(this)
                    .setTitle("Delete Category")
                    .setMessage("Are you sure you want to delete ${category.name}?")
                    .setPositiveButton("Delete") { _, _ ->
                        model.deleteCategory(category.id)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
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

            binds.savingsGoalTitle.text = "Savings Goal"
            binds.savingsGoalText.text = "${goal.name}: ${CurrencyFormatter.format(goal.savedAmount)} / ${CurrencyFormatter.format(goal.targetAmount)}"
            binds.currentSavingsText.text = CurrencyFormatter.format(goal.savedAmount)
            binds.maxSavingsText.text = CurrencyFormatter.format(goal.targetAmount)
            binds.savingsPercentageText.text = "$percent%"
            binds.savingsProgressBar.progress = percent

            binds.savingsGoalDate.text = goal.targetDate?.let {
                val dateFormat = android.text.format.DateFormat.getMediumDateFormat(this)
                "Target date: ${dateFormat.format(it)}"
            } ?: ""

            // Show/hide contribute button based on whether goal is reached
            binds.contributeButton.isEnabled = goal.savedAmount < goal.targetAmount
        } else {
            binds.savingsGoalTitle.text = "Savings Goal"
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

        // Only use user categories, do not add preset ones
        val sortedCategories = categories.sortedBy { it.name }

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
        setupStatusBar()
        setupToolbar()
        setupSwipeRefresh()
    }

    private fun setupStatusBar() {
        window.statusBarColor = getColor(R.color.primary)
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and 
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
    }

    private fun showAchievementSnackbar(achievement: Achievement) {
        val message = "🎉 Achievement Unlocked: ${achievement.title} (+${achievement.boosterBucksReward} BB)"
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .setAction("View") {
                showAchievementDetailsDialog(achievement)
            }
            .show()
    }

    private fun showAchievementDetailsDialog(achievement: Achievement) {
        MaterialAlertDialogBuilder(this)
            .setTitle(achievement.title)
            .setMessage("${achievement.description}\n\nCompleted on: ${achievement.completedAt?.toDate() ?: "N/A"}")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun setupSavingsGoalCard() {
        binds.manageGoalsButton.setOnClickListener {
            val intent = Intent(this, ManageGoalsActivity::class.java)
            startActivity(intent)
        }

        binds.contributeButton.setOnClickListener {
            showContributeDialog()
        }
    }

    private fun showContributeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_contribute_savings, null)
        val amountInput = dialogView.findViewById<TextInputEditText>(R.id.amountInput)
        val amountLayout = dialogView.findViewById<TextInputLayout>(R.id.amountLayout)

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialogView.findViewById<MaterialButton>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<MaterialButton>(R.id.confirmButton).setOnClickListener {
            val amountStr = amountInput.text.toString()
            if (amountStr.isBlank()) {
                amountLayout.error = "Please enter an amount"
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                amountLayout.error = "Please enter a valid amount"
                return@setOnClickListener
            }

            // Get the first goal from the current UI state
            val currentState = model.uiState.value
            if (currentState is DashboardUiState.Updated) {
                val goal = currentState.savingsGoals?.firstOrNull()
                if (goal != null) {
                    model.contributeToSavingsGoal(goal.id, amount)
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    private fun showEditCategoryDialog(category: Category) {
        if (!category.isEditable) {
            Snackbar.make(binds.root, "This category cannot be edited", Snackbar.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)

        val nameInput = dialogView.findViewById<TextInputLayout>(R.id.nameInput)
        val descriptionInput = dialogView.findViewById<TextInputLayout>(R.id.descriptionInput)
        val typeInput = dialogView.findViewById<TextInputLayout>(R.id.typeInput)
        val typeDropdown = typeInput.editText as? AutoCompleteTextView
        val minInput = dialogView.findViewById<TextInputLayout>(R.id.minBudgetInput)
        val maxInput = dialogView.findViewById<TextInputLayout>(R.id.maxBudgetInput)
        val iconChipGroup = dialogView.findViewById<ChipGroup>(R.id.iconChipGroup)
        val colorChipGroup = dialogView.findViewById<ChipGroup>(R.id.colorChipGroup)
        val activeSwitch = dialogView.findViewById<SwitchMaterial>(R.id.activeSwitch)

        // Populate fields
        nameInput.editText?.setText(category.name)
        descriptionInput.editText?.setText(category.description)
        typeDropdown?.setText(category.type.name, false)
        minInput.editText?.setText(category.minBudget.toString())
        maxInput.editText?.setText(category.maxBudget.toString())
        activeSwitch.isChecked = category.isActive

        // Set up type dropdown
        val types = CategoryType.values().filter { it != CategoryType.SAVINGS }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, types)
        typeDropdown?.setAdapter(adapter)

        // Set current selections
        iconChipGroup.check(
            when (category.icon) {
                "ic_savings" -> R.id.iconSavings
                "ic_utilities" -> R.id.iconUtilities
                "ic_error" -> R.id.iconEmergency
                "ic_income" -> R.id.iconIncome
                "ic_expense" -> R.id.iconExpense
                else -> R.id.iconCategory
            }
        )

        colorChipGroup.check(
            when (category.color) {
                "colorBlue" -> R.id.colorBlue
                "colorRed" -> R.id.colorRed
                "colorPurple" -> R.id.colorPurple
                "colorOrange" -> R.id.colorOrange
                else -> R.id.colorGreen
            }
        )

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Edit Category")
            .setView(dialogView)
            .setPositiveButton("Save", null) // Set to null initially
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = nameInput.editText?.text.toString()
                val description = descriptionInput.editText?.text.toString()
                val type = typeDropdown?.text.toString()
                val min = minInput.editText?.text.toString().toDoubleOrNull() ?: 0.0
                val max = maxInput.editText?.text.toString().toDoubleOrNull() ?: 0.0
                val isActive = activeSwitch.isChecked

                if (name.isBlank()) {
                    nameInput.error = "Name is required"
                    return@setOnClickListener
                }

                if (type.isBlank()) {
                    typeInput.error = "Type is required"
                    return@setOnClickListener
                }

                if (max < min) {
                    maxInput.error = "Maximum budget must be greater than minimum budget"
                    return@setOnClickListener
                }

                val selectedIconChip = iconChipGroup.findViewById<Chip>(iconChipGroup.checkedChipId)
                val selectedColorChip = colorChipGroup.findViewById<Chip>(colorChipGroup.checkedChipId)

                if (selectedIconChip == null || selectedColorChip == null) {
                    Snackbar.make(binds.root, "Please select an icon and color", Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Validate icon and color based on category type
                val categoryType = CategoryType.valueOf(type)
                val isValidIcon = when (categoryType) {
                    CategoryType.INCOME -> selectedIconChip.id == R.id.iconIncome
                    CategoryType.EXPENSE -> selectedIconChip.id == R.id.iconExpense
                    else -> true // Allow any icon for other types
                }

                val isValidColor = when (categoryType) {
                    CategoryType.INCOME -> selectedColorChip.id == R.id.colorGreen
                    CategoryType.EXPENSE -> selectedColorChip.id == R.id.colorRed
                    else -> true // Allow any color for other types
                }

                if (!isValidIcon) {
                    Snackbar.make(binds.root, 
                        "Income categories must use the income icon", 
                        Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (!isValidColor) {
                    Snackbar.make(binds.root, 
                        "Income categories must use green color, Expense categories must use red color", 
                        Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val updatedCategory = category.copy(
                    name = name,
                    type = CategoryType.valueOf(type),
                    icon = when (selectedIconChip.id) {
                        R.id.iconSavings -> "ic_savings"
                        R.id.iconUtilities -> "ic_utilities"
                        R.id.iconEmergency -> "ic_error"
                        R.id.iconIncome -> "ic_income"
                        R.id.iconExpense -> "ic_expense"
                        else -> "ic_category"
                    },
                    color = when (selectedColorChip.id) {
                        R.id.colorBlue -> "colorBlue"
                        R.id.colorRed -> "colorRed"
                        R.id.colorPurple -> "colorPurple"
                        R.id.colorOrange -> "colorOrange"
                        else -> "colorGreen"
                    },
                    description = description,
                    minBudget = min,
                    maxBudget = max,
                    isActive = isActive
                )
                model.updateCategory(updatedCategory)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showDeleteConfirmationDialog(category: Category) {
        if (!category.isEditable) {
            Snackbar.make(binds.root, "This category cannot be deleted", Snackbar.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Category")
            .setMessage("Are you sure you want to delete ${category.name}?")
            .setPositiveButton("Delete") { _, _ ->
                model.deleteCategory(category.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}