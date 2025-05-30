package vc.prog3c.poe.ui.views

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import vc.prog3c.poe.databinding.ActivityManageGoalsBinding
import vc.prog3c.poe.ui.viewmodels.GoalViewModel

class ManageGoalsActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var vBinds: ActivityManageGoalsBinding
    private lateinit var vModel: GoalViewModel


    // --- Lifecycle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBindings()
        setupLayoutUi()
        setupClickListeners()

        vModel = ViewModelProvider(this)[GoalViewModel::class.java]

        observeViewModel()
    }


    // --- ViewModel


    private fun observeViewModel() {
        vModel.error.observe(this) { error ->
            error?.let {
                Snackbar.make(vBinds.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }


    // --- Internals


    private fun validateForm(): Boolean {
        var isValid = true

        // Validate minimum goal
        val minGoalText = vBinds.minGoalInput.text.toString()
        if (minGoalText.isBlank()) {
            vBinds.minGoalLayout.error = "Minimum goal is required"
            isValid = false
        } else {
            try {
                val minGoal = minGoalText.toDouble()
                if (minGoal <= 0) {
                    vBinds.minGoalLayout.error = "Minimum goal must be greater than 0"
                    isValid = false
                } else {
                    vBinds.minGoalLayout.error = null
                }
            } catch (e: NumberFormatException) {
                vBinds.minGoalLayout.error = "Invalid amount format"
                isValid = false
            }
        }

        // Validate maximum goal
        val maxGoalText = vBinds.maxGoalInput.text.toString()
        if (maxGoalText.isBlank()) {
            vBinds.maxGoalLayout.error = "Maximum goal is required"
            isValid = false
        } else {
            try {
                val maxGoal = maxGoalText.toDouble()
                if (maxGoal <= 0) {
                    vBinds.maxGoalLayout.error = "Maximum goal must be greater than 0"
                    isValid = false
                } else {
                    vBinds.maxGoalLayout.error = null
                }
            } catch (e: NumberFormatException) {
                vBinds.maxGoalLayout.error = "Invalid amount format"
                isValid = false
            }
        }

        // Validate monthly budget
        val budgetText = vBinds.budgetInput.text.toString()
        if (budgetText.isBlank()) {
            vBinds.budgetLayout.error = "Monthly budget is required"
            isValid = false
        } else {
            try {
                val budget = budgetText.toDouble()
                if (budget <= 0) {
                    vBinds.budgetLayout.error = "Monthly budget must be greater than 0"
                    isValid = false
                } else {
                    vBinds.budgetLayout.error = null
                }
            } catch (e: NumberFormatException) {
                vBinds.budgetLayout.error = "Invalid amount format"
                isValid = false
            }
        }

        return isValid
    }


    private fun saveForm() {
        if (validateForm()) {
            val minGoal = vBinds.minGoalInput.text.toString().toDoubleOrNull() ?: 0.0
            val maxGoal = vBinds.maxGoalInput.text.toString().toDoubleOrNull() ?: 0.0
            val monthlyBudget = vBinds.budgetInput.text.toString().toDoubleOrNull() ?: 0.0

            if (vModel.validateGoal(minGoal, maxGoal, monthlyBudget)) {
                vModel.saveValidatedGoalToFirestore(minGoal, maxGoal, monthlyBudget) { success ->
                    if (success) {
                        Toast.makeText(this, "Goals updated successfully", Toast.LENGTH_SHORT)
                            .show()
                        finish()
                    } else {
                        Snackbar.make(vBinds.root, "Failed to update goals", Snackbar.LENGTH_LONG)
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
            vBinds.saveButton.id -> saveForm()
        }
    }


    private fun setupClickListeners() {
        vBinds.saveButton.setOnClickListener(this)
    }


    // --- UI Configuration


    private fun setupToolbar() {
        setSupportActionBar(vBinds.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Manage Goals"
    }


    // --- UI


    private fun setupBindings() {
        vBinds = ActivityManageGoalsBinding.inflate(layoutInflater)
    }


    private fun setupLayoutUi() {
        setContentView(vBinds.root)
        enableEdgeToEdge()

        // Internal configurations

        setupToolbar()
        saveForm()
    }
} 