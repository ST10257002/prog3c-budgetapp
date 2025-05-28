package vc.prog3c.poe.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import vc.prog3c.poe.databinding.ViewManageGoalsBinding
import vc.prog3c.poe.ui.viewmodels.DashboardViewModel

class ManageGoalsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ViewManageGoalsBinding.inflate(LayoutInflater.from(context), this, true)
    private var viewModel: DashboardViewModel? = null
    private var currentGoalId: String? = null

    fun initialize(viewModel: DashboardViewModel, lifecycleOwner: LifecycleOwner) {
        this.viewModel = viewModel

        setupListeners()
        observeGoals(lifecycleOwner)
    }

    private fun setupListeners() {
        binding.saveButton.setOnClickListener {
            val name = binding.goalNameInput.text?.toString()?.trim()
            val min = binding.minGoalInput.text?.toString()?.toDoubleOrNull()
            val max = binding.maxGoalInput.text?.toString()?.toDoubleOrNull()
            val budget = binding.budgetInput.text?.toString()?.toDoubleOrNull()

            if (!name.isNullOrBlank() && min != null && max != null && budget != null) {
                currentGoalId?.let { goalId ->
                    viewModel?.updateSavingsGoal(goalId, min, max, budget, name)
                }
            } else {
                // 🔺 You could show an error Snackbar or Toast here
                binding.goalNameInput.error = if (name.isNullOrBlank()) "Required" else null
                if (min == null) binding.minGoalInput.error = "Invalid"
                if (max == null) binding.maxGoalInput.error = "Invalid"
                if (budget == null) binding.budgetInput.error = "Invalid"
            }
        }

        binding.cancelButton.setOnClickListener {
            this.visibility = View.GONE
        }
    }

    private fun observeGoals(lifecycleOwner: LifecycleOwner) {
        viewModel?.savingsGoals?.observe(lifecycleOwner) { goals ->
            if (goals.isNotEmpty()) {
                val goal = goals.first()
                currentGoalId = goal.id

                binding.goalNameInput.setText(goal.name)
                binding.minGoalInput.setText(goal.minMonthlyGoal.toString())
                binding.maxGoalInput.setText(goal.maxMonthlyGoal.toString())
                binding.budgetInput.setText(goal.monthlyBudget.toString())
            } else {
                clearInputs()
            }
        }
    }

    private fun clearInputs() {
        currentGoalId = null
        binding.goalNameInput.setText("")
        binding.minGoalInput.setText("")
        binding.maxGoalInput.setText("")
        binding.budgetInput.setText("")
    }
}
