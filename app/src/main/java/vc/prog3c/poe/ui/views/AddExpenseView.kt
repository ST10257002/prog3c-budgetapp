package vc.prog3c.poe.ui.views

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import vc.prog3c.poe.R
import vc.prog3c.poe.databinding.ActivityAddExpenseBinding
import vc.prog3c.poe.ui.viewmodels.Expense
import vc.prog3c.poe.ui.viewmodels.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class AddExpenseView : AppCompatActivity() {
    private lateinit var binding: ActivityAddExpenseBinding
    private lateinit var viewModel: ExpenseViewModel
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val selectedPhotos = mutableListOf<String>() // TODO: Replace with actual photo handling

    private val expenseCategories = listOf(
        "Food & Dining",
        "Transportation",
        "Housing",
        "Utilities",
        "Entertainment",
        "Shopping",
        "Healthcare",
        "Education",
        "Travel",
        "Other"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ExpenseViewModel::class.java]

        setupToolbar()
        setupCategoryDropdown()
        setupTimePickers()
        setupPhotoButtons()
        setupPhotoRecyclerView()
        setupSaveButton()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun setupCategoryDropdown() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, expenseCategories)
        binding.categoryInput.setAdapter(adapter)

        binding.addCategoryButton.setOnClickListener {
            // TODO: Implement custom category addition
            Snackbar.make(binding.root, "Custom category feature coming soon", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun setupTimePickers() {
        binding.startTimeInput.setOnClickListener {
            showTimePicker { time ->
                binding.startTimeInput.setText(timeFormatter.format(time))
            }
        }

        binding.endTimeInput.setOnClickListener {
            showTimePicker { time ->
                binding.endTimeInput.setText(timeFormatter.format(time))
            }
        }
    }

    private fun showTimePicker(onTimeSelected: (Date) -> Unit) {
        TimePickerDialog(
            this,
            { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                onTimeSelected(calendar.time)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun setupPhotoButtons() {
        binding.addPhotoButton.setOnClickListener {
            // TODO: Implement photo selection from gallery
            Snackbar.make(binding.root, "Photo selection coming soon", Snackbar.LENGTH_SHORT).show()
        }

        binding.capturePhotoButton.setOnClickListener {
            // TODO: Implement photo capture
            Snackbar.make(binding.root, "Photo capture coming soon", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun setupPhotoRecyclerView() {
        binding.photoRecyclerView.layoutManager = LinearLayoutManager(this)
        // TODO: Implement photo adapter
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            if (validateInputs()) {
                saveExpense()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Validate description
        val description = binding.descriptionInput.text.toString()
        if (description.isBlank()) {
            binding.descriptionLayout.error = "Description is required"
            isValid = false
        } else {
            binding.descriptionLayout.error = null
        }

        // Validate amount
        val amountText = binding.amountInput.text.toString()
        if (amountText.isBlank()) {
            binding.amountLayout.error = "Amount is required"
            isValid = false
        } else {
            try {
                val amount = amountText.toDouble()
                if (amount <= 0) {
                    binding.amountLayout.error = "Amount must be greater than 0"
                    isValid = false
                } else {
                    binding.amountLayout.error = null
                }
            } catch (e: NumberFormatException) {
                binding.amountLayout.error = "Invalid amount format"
                isValid = false
            }
        }

        // Validate times
        if (binding.startTimeInput.text.isNullOrBlank()) {
            binding.startTimeLayout.error = "Start time is required"
            isValid = false
        } else {
            binding.startTimeLayout.error = null
        }

        if (binding.endTimeInput.text.isNullOrBlank()) {
            binding.endTimeLayout.error = "End time is required"
            isValid = false
        } else {
            binding.endTimeLayout.error = null
        }

        // Validate category
        val category = binding.categoryInput.text.toString()
        if (category.isBlank()) {
            binding.categoryLayout.error = "Category is required"
            isValid = false
        } else if (!expenseCategories.contains(category)) {
            binding.categoryLayout.error = "Please select a valid category"
            isValid = false
        } else {
            binding.categoryLayout.error = null
        }

        return isValid
    }

    private fun saveExpense() {
        val description = binding.descriptionInput.text.toString()
        val amount = binding.amountInput.text.toString().toDouble()
        val category = binding.categoryInput.text.toString()
        val startTime = binding.startTimeInput.text.toString()
        val endTime = binding.endTimeInput.text.toString()

        val expense = Expense(
            id = UUID.randomUUID().toString(),
            amount = amount,
            category = category,
            date = calendar.time,
            description = description
        )

        viewModel.addExpense(expense)
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.saveButton.isEnabled = !isLoading
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
                    .setAction("Retry") { viewModel.retryLastOperation() }
                    .show()
            }
        }

        viewModel.saveSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Expense saved successfully", Toast.LENGTH_SHORT).show()
                finish()
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