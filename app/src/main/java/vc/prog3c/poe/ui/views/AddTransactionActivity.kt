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
import com.google.firebase.Timestamp
import vc.prog3c.poe.R
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import vc.prog3c.poe.databinding.ActivityAddTransactionBinding
import vc.prog3c.poe.ui.viewmodels.TransactionViewModel
import vc.prog3c.poe.utils.TransactionState
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var viewModel: TransactionViewModel
    private var accountId: String? = null
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    private val expenseCategories = listOf("Food", "Housing", "Transport", "Entertainment", "Other")
    private val incomeCategories = listOf("Salary", "Freelance", "Gift", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[TransactionViewModel::class.java]
        accountId = intent.getStringExtra("account_id") ?: run {
            Toast.makeText(this, "Missing account ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupDropdowns()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Transaction"
    }

    private fun setupDropdowns() {
        binding.transactionTypeInput.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listOf("Income", "Expense")))
        binding.incomeForm.sourceInput.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, incomeCategories))
        binding.expenseForm.categoryInput.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, expenseCategories))
    }

    private fun setupListeners() {
        binding.transactionTypeInput.setOnItemClickListener { _, _, position, _ ->
            binding.incomeForm.root.visibility = if (position == 0) View.VISIBLE else View.GONE
            binding.expenseForm.root.visibility = if (position == 1) View.VISIBLE else View.GONE
        }

        binding.incomeForm.dateInput.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                calendar.set(year, month, day)
                binding.incomeForm.dateInput.setText(dateFormatter.format(calendar.time))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.expenseForm.startTimeInput.setOnClickListener { showTimePicker(binding.expenseForm.startTimeInput) }
        binding.expenseForm.endTimeInput.setOnClickListener { showTimePicker(binding.expenseForm.endTimeInput) }

        binding.saveButton.setOnClickListener {
            when (binding.transactionTypeInput.text.toString()) {
                "Income" -> saveIncomeTransaction()
                "Expense" -> saveExpenseTransaction()
                else -> Snackbar.make(binding.root, "Please select a transaction type", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun showTimePicker(targetView: android.widget.EditText) {
        TimePickerDialog(this, { _, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            targetView.setText(timeFormatter.format(calendar.time))
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    private fun saveIncomeTransaction() {
        val form = binding.incomeForm
        val amount = form.amountInput.text.toString().toDoubleOrNull()
        val source = form.sourceInput.text.toString()
        val description = form.descriptionInput.text?.toString() ?: ""

        if (amount == null || amount <= 0) {
            form.amountLayout.error = "Enter a valid amount"
            return
        } else form.amountLayout.error = null

        if (source.isBlank()) {
            form.sourceLayout.error = "Source is required"
            return
        } else form.sourceLayout.error = null

        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            userId = viewModel.getCurrentUserId(),
            accountId = accountId ?: "",
            type = TransactionType.INCOME,
            amount = amount,
            category = source,
            date = Timestamp(calendar.time),
            description = description
        )

        viewModel.addTransaction(transaction)
    }

    private fun saveExpenseTransaction() {
        val form = binding.expenseForm
        val amount = form.amountInput.text.toString().toDoubleOrNull()
        val category = form.categoryInput.text.toString()
        val description = form.descriptionInput.text?.toString() ?: ""

        if (amount == null || amount <= 0) {
            form.amountLayout.error = "Enter a valid amount"
            return
        } else form.amountLayout.error = null

        if (category.isBlank()) {
            form.categoryLayout.error = "Category is required"
            return
        } else form.categoryLayout.error = null

        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            userId = viewModel.getCurrentUserId(),
            accountId = accountId ?: "",
            type = TransactionType.EXPENSE,
            amount = amount,
            category = category,
            date = Timestamp(calendar.time),
            description = description
        )

        viewModel.addTransaction(transaction)
    }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is TransactionState.Success -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    finish()
                }
                is TransactionState.Error -> {
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
                TransactionState.Loading -> { /* Optional: show loading UI */ }
                TransactionState.Idle -> Unit
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
