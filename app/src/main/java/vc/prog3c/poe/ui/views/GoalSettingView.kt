package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import vc.prog3c.poe.databinding.ActivityGoalSettingBinding
import vc.prog3c.poe.ui.viewmodels.AuthViewModel

class GoalSettingView : AppCompatActivity() {
    private lateinit var binding: ActivityGoalSettingBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoalSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupSaveButton()
        observeViewModel()
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            val minGoal = binding.minGoalEditText.text.toString().toDoubleOrNull()
            val maxGoal = binding.maxGoalEditText.text.toString().toDoubleOrNull()
            val monthlyBudget = binding.budgetEditText.text.toString().toDoubleOrNull()

            if (minGoal == null || maxGoal == null || monthlyBudget == null) {
                Toast.makeText(this, "Please enter valid amounts", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (viewModel.setGoals(minGoal, maxGoal, monthlyBudget) != null) {
                startActivity(Intent(this, DashboardView::class.java))
                finish()
            } else {
                Toast.makeText(this, viewModel.error.value, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }
} 