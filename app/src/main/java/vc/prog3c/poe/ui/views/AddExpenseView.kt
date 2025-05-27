package vc.prog3c.poe.ui.views

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import vc.prog3c.poe.R
import vc.prog3c.poe.databinding.ActivityAddExpenseBinding
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import vc.prog3c.poe.ui.viewmodels.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AddExpenseView : AppCompatActivity() {
    private lateinit var binding: ActivityAddExpenseBinding
    private lateinit var viewModel: TransactionViewModel
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    private var accountId: String? = null
    private val expenseCategories = listOf(
        "Food & Dining", "Transportation", "Housing", "Utilities",
        "Entertainment", "Shopping", "Healthcare", "Education",
        "Travel", "Other"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[TransactionViewModel::class.java]
        accountId = intent.getStringExtra("account_id")

        setupToolbar()
        setupDateAndTimePickers()
        setupCategoryInput()
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

    private fun setupDateAndTimePickers() {
        binding.startTimeInput.setOnClickListener {
            showTimePicker(binding.startTimeInput)
        }
        binding.endTimeInput.setOnClickListener {
            showTimePicker(binding.endTimeInput)
        }

        binding.startTimeInput.setText(timeFormatter.format(calendar.time))
        binding.endTimeInput.setText(timeFormatter.format(calendar.time))
    }

    private fun showTimePicker(inputEditText: com.google.android.material.textfield.TextInputEditText) {
        TimePickerDialog(
            this,
            { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                inputEditText.setText(timeFormatter.format(calendar.time))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun setupCategoryInput() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            expenseCategories
        )
        binding.categoryInput.setAdapter(adapter)

        binding.addCategoryButton.setOnClickListener {
            Snackbar.make(binding.root, "Custom category feature coming soon", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun setupPhotoButtons() {
        binding.addPhotoButton.setOnClickListener {
            Snackbar.make(binding.root, "Photo selection coming soon", Snackbar.LENGTH_SHORT).show()
        }
        binding.capturePhotoButton.setOnClickListener {
            Snackbar.make(binding.root, "Photo capture coming soon", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun setupPhotoRecyclerView() {
        binding.photoRecyclerView.layoutManager = LinearLayoutManager(this)
        // Photo adapter not implemented in this example
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

        // Description
        val description = binding.descriptionInput.text.toString()
        if (description.isBlank()) {
            binding.descriptionLayout.error = "Description is required"
            isValid = false
        } else {
            binding.descriptionLayout.error = null
        }

        // Amount
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

        // Times
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

        // Category
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
        val amount = binding.amountInput.text.toString().toDouble()
        val category = binding.categoryInput.text.toString()
        val description = binding.descriptionInput.text.toString().takeIf { it.isNotBlank() }
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val account = accountId ?: return

        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            userId = userId,
            accountId = account,
            type = TransactionType.EXPENSE,
            amount = amount,
            category = category,
            date = Timestamp(calendar.time),
            description = description
        )
        viewModel.addTransaction(transaction)
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.saveButton.isEnabled = !isLoading
        }
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
