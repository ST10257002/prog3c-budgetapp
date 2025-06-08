package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import vc.prog3c.poe.databinding.ActivityGoalSettingBinding
import vc.prog3c.poe.ui.viewmodels.GoalViewModel

class GoalSettingView : AppCompatActivity(), View.OnClickListener {

    private lateinit var binds: ActivityGoalSettingBinding
    private lateinit var model: GoalViewModel


    // --- Lifecycle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBindings()
        setupLayoutUi()
        setupClickListeners()

        model = ViewModelProvider(this)[GoalViewModel::class.java]

        observeViewModel()
    }


    // --- ViewModel


    private fun observeViewModel() {
        model.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }


    // --- Internals


    private fun saveForm() {
        val minGoal = binds.minGoalEditText.text.toString().toDoubleOrNull()
        val maxGoal = binds.maxGoalEditText.text.toString().toDoubleOrNull()
        val monthlyBudget = binds.budgetEditText.text.toString().toDoubleOrNull()
        val goalName = "Monthly Savings Goal" // Default name for goal setting view

        if (minGoal == null || maxGoal == null || monthlyBudget == null) {
            Toast.makeText(this, "Please enter valid amounts", Toast.LENGTH_SHORT).show()
            return
        }

        if (model.validateGoal(minGoal, maxGoal, monthlyBudget)) {
            model.saveValidatedGoalToFirestore(goalName, minGoal, maxGoal, monthlyBudget) { success ->
                if (success) {
                    Toast.makeText(this, "Goal saved!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, DashboardView::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Failed to save goal", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, model.error.value ?: "Invalid goal", Toast.LENGTH_SHORT).show()
        }
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


    // --- UI


    private fun setupBindings() {
        binds = ActivityGoalSettingBinding.inflate(layoutInflater)
    }


    private fun setupLayoutUi() {
        setContentView(binds.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binds.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
