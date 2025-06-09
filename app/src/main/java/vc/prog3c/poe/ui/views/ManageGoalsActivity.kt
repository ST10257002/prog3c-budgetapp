package vc.prog3c.poe.ui.views
/**
 * @reference Material TextInputLayout with Error Handling: https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout
 * @reference Firebase Firestore - Read & Update Documents: https://firebase.google.com/docs/firestore/manage-data/add-data
 * @reference Snackbar and Toast for Feedback: https://developer.android.com/guide/topics/ui/notifiers/toasts
 */

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import vc.prog3c.poe.R
import vc.prog3c.poe.databinding.ActivityManageGoalsBinding
import vc.prog3c.poe.ui.viewmodels.GoalViewModel
import vc.prog3c.poe.core.utils.CurrencyFormatter

class ManageGoalsActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binds: ActivityManageGoalsBinding
    private lateinit var model: GoalViewModel


    // --- Lifecycle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBindings()
        setupLayoutUi()
        setupClickListeners()

        model = ViewModelProvider(this)[GoalViewModel::class.java]

        observeViewModel()
        loadCurrentGoal()
    }


    // --- ViewModel


    private fun observeViewModel() {
        model.error.observe(this) { error ->
            error?.let {
                Snackbar.make(binds.root, it, Snackbar.LENGTH_LONG).show()
            }
        }

        model.isLoading.observe(this) { isLoading ->
            binds.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }


    private fun loadCurrentGoal() {
        model.loadCurrentGoal { goal ->
            goal?.let {
                binds.goalNameInput.setText(it.name)
                binds.minGoalInput.setText(it.minMonthlyGoal.toString())
                binds.maxGoalInput.setText(it.maxMonthlyGoal.toString())
                binds.budgetInput.setText(it.monthlyBudget.toString())
            }
        }
    }


    // --- Internals


    private fun validateForm(): Boolean {
        var isValid = true

        // Validate goal name
        val goalName = binds.goalNameInput.text.toString()
        if (goalName.isBlank()) {
            binds.goalNameLayout.error = "Goal name is required"
            isValid = false
        } else {
            binds.goalNameLayout.error = null
        }

        // Validate minimum goal
        val minGoalText = binds.minGoalInput.text.toString()
        if (minGoalText.isBlank()) {
            binds.minGoalLayout.error = "Minimum goal is required"
            isValid = false
        } else {
            try {
                val minGoal = minGoalText.toDouble()
                if (minGoal <= 0) {
                    binds.minGoalLayout.error = "Minimum goal must be greater than 0"
                    isValid = false
                } else {
                    binds.minGoalLayout.error = null
                }
            } catch (e: NumberFormatException) {
                binds.minGoalLayout.error = "Invalid amount format"
                isValid = false
            }
        }

        // Validate maximum goal
        val maxGoalText = binds.maxGoalInput.text.toString()
        if (maxGoalText.isBlank()) {
            binds.maxGoalLayout.error = "Maximum goal is required"
            isValid = false
        } else {
            try {
                val maxGoal = maxGoalText.toDouble()
                if (maxGoal <= 0) {
                    binds.maxGoalLayout.error = "Maximum goal must be greater than 0"
                    isValid = false
                } else {
                    binds.maxGoalLayout.error = null
                }
            } catch (e: NumberFormatException) {
                binds.maxGoalLayout.error = "Invalid amount format"
                isValid = false
            }
        }

        // Validate monthly budget
        val budgetText = binds.budgetInput.text.toString()
        if (budgetText.isBlank()) {
            binds.budgetLayout.error = "Monthly budget is required"
            isValid = false
        } else {
            try {
                val budget = budgetText.toDouble()
                if (budget <= 0) {
                    binds.budgetLayout.error = "Monthly budget must be greater than 0"
                    isValid = false
                } else {
                    binds.budgetLayout.error = null
                }
            } catch (e: NumberFormatException) {
                binds.budgetLayout.error = "Invalid amount format"
                isValid = false
            }
        }

        return isValid
    }


    private fun saveForm() {
        if (validateForm()) {
            val goalName = binds.goalNameInput.text.toString()
            val minGoal = binds.minGoalInput.text.toString().toDoubleOrNull() ?: 0.0
            val maxGoal = binds.maxGoalInput.text.toString().toDoubleOrNull() ?: 0.0
            val monthlyBudget = binds.budgetInput.text.toString().toDoubleOrNull() ?: 0.0

            if (model.validateGoal(minGoal, maxGoal, monthlyBudget)) {
                model.saveValidatedGoalToFirestore(goalName, minGoal, maxGoal, monthlyBudget) { success ->
                    if (success) {
                        Toast.makeText(this, "Goals updated successfully", Toast.LENGTH_SHORT)
                            .show()
                        finish()
                    } else {
                        Snackbar.make(binds.root, "Failed to update goals", Snackbar.LENGTH_LONG)
                            .show()
                    }
                }
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    // --- Event Handlers


    override fun onClick(view: View?) {
        when (view?.id) {
            binds.saveButton.id -> saveForm()
        }
    }


    private fun setupClickListeners() {
        binds.saveButton.setOnClickListener(this)
    }


    // --- UI Configuration


    private fun setupToolbar() {
        setSupportActionBar(binds.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Manage Goals"
    }


    // --- UI


    private fun setupBindings() {
        binds = ActivityManageGoalsBinding.inflate(layoutInflater)
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
    }

    private fun setupStatusBar() {
        window.statusBarColor = getColor(R.color.primary)
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and 
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
    }
} 