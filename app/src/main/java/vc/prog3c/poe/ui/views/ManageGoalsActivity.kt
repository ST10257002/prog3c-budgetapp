package vc.prog3c.poe.ui.views

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import vc.prog3c.poe.databinding.ActivityManageGoalsBinding
import vc.prog3c.poe.ui.viewmodels.GoalViewModel

class ManageGoalsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManageGoalsBinding
    private lateinit var viewModel: GoalViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageGoalsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[GoalViewModel::class.java]

        setupToolbar()
        setupForm()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Manage Goals"
    }

    private fun setupForm() {
        // Setup save button
        binding.saveButton.setOnClickListener {
            if (validateForm()) {
                val minGoal = binding.minGoalInput.text.toString().toDoubleOrNull() ?: 0.0
                val maxGoal = binding.maxGoalInput.text.toString().toDoubleOrNull() ?: 0.0
                val monthlyBudget = binding.budgetInput.text.toString().toDoubleOrNull() ?: 0.0

                if (viewModel.validateGoal(minGoal, maxGoal, monthlyBudget)) {
                    viewModel.saveValidatedGoalToFirestore(minGoal, maxGoal, monthlyBudget) { success ->
                        if (success) {
                            Toast.makeText(this, "Goals updated successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Snackbar.make(binding.root, "Failed to update goals", Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Validate minimum goal
        val minGoalText = binding.minGoalInput.text.toString()
        if (minGoalText.isBlank()) {
            binding.minGoalLayout.error = "Minimum goal is required"
            isValid = false
        } else {
            try {
                val minGoal = minGoalText.toDouble()
                if (minGoal <= 0) {
                    binding.minGoalLayout.error = "Minimum goal must be greater than 0"
                    isValid = false
                } else {
                    binding.minGoalLayout.error = null
                }
            } catch (e: NumberFormatException) {
                binding.minGoalLayout.error = "Invalid amount format"
                isValid = false
            }
        }

        // Validate maximum goal
        val maxGoalText = binding.maxGoalInput.text.toString()
        if (maxGoalText.isBlank()) {
            binding.maxGoalLayout.error = "Maximum goal is required"
            isValid = false
        } else {
            try {
                val maxGoal = maxGoalText.toDouble()
                if (maxGoal <= 0) {
                    binding.maxGoalLayout.error = "Maximum goal must be greater than 0"
                    isValid = false
                } else {
                    binding.maxGoalLayout.error = null
                }
            } catch (e: NumberFormatException) {
                binding.maxGoalLayout.error = "Invalid amount format"
                isValid = false
            }
        }

        // Validate monthly budget
        val budgetText = binding.budgetInput.text.toString()
        if (budgetText.isBlank()) {
            binding.budgetLayout.error = "Monthly budget is required"
            isValid = false
        } else {
            try {
                val budget = budgetText.toDouble()
                if (budget <= 0) {
                    binding.budgetLayout.error = "Monthly budget must be greater than 0"
                    isValid = false
                } else {
                    binding.budgetLayout.error = null
                }
            } catch (e: NumberFormatException) {
                binding.budgetLayout.error = "Invalid amount format"
                isValid = false
            }
        }

        return isValid
    }

    private fun observeViewModel() {
        viewModel.error.observe(this) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
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
} 