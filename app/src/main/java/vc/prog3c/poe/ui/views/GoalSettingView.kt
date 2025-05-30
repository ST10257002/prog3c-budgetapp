package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import vc.prog3c.poe.databinding.ActivityGoalSettingBinding
import vc.prog3c.poe.ui.viewmodels.GoalViewModel

class GoalSettingView : AppCompatActivity(), View.OnClickListener {

    private lateinit var vBinds: ActivityGoalSettingBinding
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
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }


    // --- Internals


    private fun saveForm() {
        val minGoal = vBinds.minGoalEditText.text.toString().toDoubleOrNull()
        val maxGoal = vBinds.maxGoalEditText.text.toString().toDoubleOrNull()
        val monthlyBudget = vBinds.budgetEditText.text.toString().toDoubleOrNull()

        if (minGoal == null || maxGoal == null || monthlyBudget == null) {
            Toast.makeText(this, "Please enter valid amounts", Toast.LENGTH_SHORT).show()
            return
        }

        if (vModel.validateGoal(minGoal, maxGoal, monthlyBudget)) {
            vModel.saveValidatedGoalToFirestore(minGoal, maxGoal, monthlyBudget) { success ->
                if (success) {
                    Toast.makeText(this, "Goal saved!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, DashboardView::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Failed to save goal", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, vModel.error.value ?: "Invalid goal", Toast.LENGTH_SHORT).show()
        }
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


    // --- UI


    private fun setupBindings() {
        vBinds = ActivityGoalSettingBinding.inflate(layoutInflater)
    }


    private fun setupLayoutUi() {
        setContentView(vBinds.root)
        enableEdgeToEdge()
    }
}
