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

        // Save
        binding.saveButton.setOnClickListener {
            val name = binding.goalNameInput.text?.toString()?.trim()
            val min = binding.minGoalInput.text?.toString()?.toDoubleOrNull()
            val max = binding.maxGoalInput.text?.toString()?.toDoubleOrNull()
            val budget = binding.budgetInput.text?.toString()?.toDoubleOrNull()

            if (currentGoalId != null && !name.isNullOrBlank() && min != null && max != null && budget != null) {
                // Update all fields
                viewModel.updateSavingsGoal(
                    goalId = currentGoalId!!,
                    min = min,
                    max = max,
                    budget = budget,
                    name = name // New parameter
                )
            } else {
                // Optionally, show error
            }
        }

        // Cancel
        binding.cancelButton.setOnClickListener {
            this.visibility = View.GONE
        }

        // Observe and populate fields
        viewModel.savingsGoals.observe(lifecycleOwner) { goals ->
            if (goals.isNotEmpty()) {
                val goal = goals[0]
                currentGoalId = goal.id
                binding.goalNameInput.setText(goal.name)
                binding.minGoalInput.setText(goal.minMonthlyGoal.toString())
                binding.maxGoalInput.setText(goal.maxMonthlyGoal.toString())
                binding.budgetInput.setText(goal.monthlyBudget.toString())
            } else {
                binding.goalNameInput.setText("")
                binding.minGoalInput.setText("")
                binding.maxGoalInput.setText("")
                binding.budgetInput.setText("")
                currentGoalId = null
            }
        }
    }
}
