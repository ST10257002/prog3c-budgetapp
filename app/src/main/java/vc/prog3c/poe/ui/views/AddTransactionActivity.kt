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
import vc.prog3c.poe.databinding.ActivityAddTransactionBinding
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import vc.prog3c.poe.ui.viewmodels.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var viewModel: TransactionViewModel
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    private var accountId: String? = null
    private val selectedPhotos = mutableListOf<String>() // Placeholder for photo handling

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

    private val incomeCategories = listOf(
        "Salary",
        "Freelance",
        "Investments",
        "Gifts",
        "Refunds",
        "Other"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[TransactionViewModel::class.java]
        accountId = intent.getStringExtra("account_id")

        setupToolbar()
        setupTransactionTypeDropdown()
        setupSaveButton()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Add Transaction"
    }

    private fun setupTransactionTypeDropdown() {
        val transactionTypes = listOf("Income", "Expense")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            transactionTypes
        )
        binding.transactionTypeInput.setAdapter(adapter)

        binding.transactionTypeInput.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> showIncomeForm()
                1 -> showExpenseForm()
            }
        }
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            when (binding.transactionTypeInput.text.toString()) {
                "Income" -> if (validateIncomeForm()) saveIncomeTransaction()
                "Expense" -> if (validateExpenseForm()) saveExpenseTransaction()
                else -> {
                    Snackbar.make(binding.root, "Please select a transaction type", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showIncomeForm() {
        binding.incomeForm.root.visibility = View.VISIBLE
        binding.expenseForm.root.visibility = View.GONE
        setupIncomeForm()
    }

    private fun showExpenseForm() {
        binding.incomeForm.root.visibility = View.GONE
        binding.expenseForm.root.visibility = View.VISIBLE
        setupExpenseForm()
    }

    private fun setupIncomeForm() {
        val incomeForm = binding.incomeForm
        
        // Setup source dropdown
        val sourceAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, incomeCategories)
        incomeForm.sourceInput.setAdapter(sourceAdapter)

        // Setup date picker
        incomeForm.dateInput.setOnClickListener {
            showDatePicker(incomeForm.dateInput)
        }
        incomeForm.dateInput.setText(dateFormatter.format(calendar.time))
    }

    private fun setupExpenseForm() {
        val expenseForm = binding.expenseForm
        
        // Setup category dropdown
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, expenseCategories)
        expenseForm.categoryInput.setAdapter(categoryAdapter)

        // Setup time pickers
        expenseForm.startTimeInput.setOnClickListener {
            showTimePicker(expenseForm.startTimeInput)
        }
        expenseForm.endTimeInput.setOnClickListener {
            showTimePicker(expenseForm.endTimeInput)
        }
        expenseForm.startTimeInput.setText(timeFormatter.format(calendar.time))
        expenseForm.endTimeInput.setText(timeFormatter.format(calendar.time))

        // Setup photo buttons
        expenseForm.addPhotoButton.setOnClickListener {
            // TODO: Implement photo selection from gallery
            Snackbar.make(binding.root, "Photo selection coming soon", Snackbar.LENGTH_SHORT).show()
        }

        expenseForm.capturePhotoButton.setOnClickListener {
            // TODO: Implement photo capture
            Snackbar.make(binding.root, "Photo capture coming soon", Snackbar.LENGTH_SHORT).show()
        }

        // Setup photo recycler view
        expenseForm.photoRecyclerView.layoutManager = LinearLayoutManager(this)
        // TODO: Implement photo adapter
    }

    private fun showDatePicker(dateInput: com.google.android.material.textfield.TextInputEditText) {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                dateInput.setText(dateFormatter.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
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

    private fun validateIncomeForm(): Boolean {
        val incomeForm = binding.incomeForm
        var isValid = true

        // Validate amount
        val amountText = incomeForm.amountInput.text.toString()
        if (amountText.isBlank()) {
            incomeForm.amountLayout.error = "Amount is required"
            isValid = false
        } else {
            try {
                val amount = amountText.toDouble()
                if (amount <= 0) {
                    incomeForm.amountLayout.error = "Amount must be greater than 0"
                    isValid = false
                } else {
                    incomeForm.amountLayout.error = null
                }
            } catch (e: NumberFormatException) {
                incomeForm.amountLayout.error = "Invalid amount format"
                isValid = false
            }
        }

        // Validate source
        val source = incomeForm.sourceInput.text.toString()
        if (source.isBlank()) {
            incomeForm.sourceLayout.error = "Source is required"
            isValid = false
        } else {
            incomeForm.sourceLayout.error = null
        }

        // Validate date
        if (incomeForm.dateInput.text.isNullOrBlank()) {
            incomeForm.dateLayout.error = "Date is required"
            isValid = false
        } else {
            incomeForm.dateLayout.error = null
        }

        return isValid
    }

    private fun validateExpenseForm(): Boolean {
        val expenseForm = binding.expenseForm
        var isValid = true

        // Validate description
        val description = expenseForm.descriptionInput.text.toString()
        if (description.isBlank()) {
            expenseForm.descriptionLayout.error = "Description is required"
            isValid = false
        } else {
            expenseForm.descriptionLayout.error = null
        }

        // Validate amount
        val amountText = expenseForm.amountInput.text.toString()
        if (amountText.isBlank()) {
            expenseForm.amountLayout.error = "Amount is required"
            isValid = false
        } else {
            try {
                val amount = amountText.toDouble()
                if (amount <= 0) {
                    expenseForm.amountLayout.error = "Amount must be greater than 0"
                    isValid = false
                } else {
                    expenseForm.amountLayout.error = null
                }
            } catch (e: NumberFormatException) {
                expenseForm.amountLayout.error = "Invalid amount format"
                isValid = false
            }
        }

        // Validate times
        if (expenseForm.startTimeInput.text.isNullOrBlank()) {
            expenseForm.startTimeLayout.error = "Start time is required"
            isValid = false
        } else {
            expenseForm.startTimeLayout.error = null
        }

        if (expenseForm.endTimeInput.text.isNullOrBlank()) {
            expenseForm.endTimeLayout.error = "End time is required"
            isValid = false
        } else {
            expenseForm.endTimeLayout.error = null
        }

        // Validate category
        val category = expenseForm.categoryInput.text.toString()
        if (category.isBlank()) {
            expenseForm.categoryLayout.error = "Category is required"
            isValid = false
        } else if (!expenseCategories.contains(category)) {
            expenseForm.categoryLayout.error = "Please select a valid category"
            isValid = false
        } else {
            expenseForm.categoryLayout.error = null
        }

        return isValid
    }

    private fun saveIncomeTransaction() {
        val incomeForm = binding.incomeForm
        try {
            val amount = incomeForm.amountInput.text.toString().toDouble()
            val category = incomeForm.sourceInput.text.toString()
            val date = calendar.time
            val description = incomeForm.descriptionInput.text.toString().takeIf { it.isNotBlank() }

            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                userId = "user1", // TODO: Get from auth service
                accountId = accountId,
                type = TransactionType.INCOME,
                amount = amount,
                category = category,
                date = date,
                description = description
            )

            viewModel.addTransaction(transaction)
        } catch (e: NumberFormatException) {
            Snackbar.make(binding.root, "Please enter a valid amount", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun saveExpenseTransaction() {
        val expenseForm = binding.expenseForm
        try {
            val amount = expenseForm.amountInput.text.toString().toDouble()
            val category = expenseForm.categoryInput.text.toString()
            val date = calendar.time
            val description = expenseForm.descriptionInput.text.toString().takeIf { it.isNotBlank() }
            
            // Parse start and end times
            val startTimeStr = expenseForm.startTimeInput.text.toString()
            val endTimeStr = expenseForm.endTimeInput.text.toString()
            val startTime = timeFormatter.parse(startTimeStr) ?: throw IllegalArgumentException("Invalid start time")
            val endTime = timeFormatter.parse(endTimeStr) ?: throw IllegalArgumentException("Invalid end time")

            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                userId = "user1", // TODO: Get from auth service
                accountId = accountId,
                type = TransactionType.EXPENSE,
                amount = amount,
                category = category,
                date = date,
                description = description,
                startTime = startTime,
                endTime = endTime,
                photoUrls = selectedPhotos
            )

            viewModel.addTransaction(transaction)
        } catch (e: NumberFormatException) {
            Snackbar.make(binding.root, "Please enter a valid amount", Snackbar.LENGTH_SHORT).show()
        } catch (e: IllegalArgumentException) {
            Snackbar.make(binding.root, e.message ?: "Invalid input", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.saveButton.isEnabled = !isLoading
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
                    .setAction("Retry") {
                        viewModel.retryLastOperation()
                    }
                    .show()
            }
        }

        viewModel.saveSuccess.observe(this) { success: Boolean ->
            if (success == true) {
                Toast.makeText(this, "Transaction added successfully", Toast.LENGTH_SHORT).show()
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