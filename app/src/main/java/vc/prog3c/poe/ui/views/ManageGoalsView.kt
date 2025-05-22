package vc.prog3c.poe.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
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

    fun initialize(viewModel: DashboardViewModel, lifecycleOwner: LifecycleOwner) {
        this.viewModel = viewModel
        
        // Set up initial values
        binding.savingsGoalInput.setText(viewModel.savingsGoal.value.toString())
        
        // Set up listeners
        binding.updateGoalButton.setOnClickListener {
            val newGoal = binding.savingsGoalInput.text.toString().toDoubleOrNull()
            if (newGoal != null) {
                // TODO: Backend Implementation Required
                // 1. Create Firestore collection 'savings_goals' with structure:
                //    - userId: string
                //    - targetAmount: number
                //    - currentAmount: number
                //    - lastUpdated: timestamp
                // 2. Implement real-time updates using Firestore listeners
                // 3. Add offline persistence support
                // 4. Implement data synchronization
                // 5. Add error handling for network issues
                viewModel.updateSavingsGoal(newGoal)
            }
        }
        
        // Observe changes
        viewModel.savingsGoal.observe(lifecycleOwner) { goal ->
            binding.savingsGoalInput.setText(goal.toString())
        }
    }
} 