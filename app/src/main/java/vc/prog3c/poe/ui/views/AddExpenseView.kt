package vc.prog3c.poe.ui.views

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.datepicker.MaterialDatePicker
import vc.prog3c.poe.databinding.ActivityAddExpenseBinding
import vc.prog3c.poe.ui.viewmodels.Expense
import vc.prog3c.poe.ui.viewmodels.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AddExpenseView : AppCompatActivity() {
    private lateinit var binding: ActivityAddExpenseBinding
    private lateinit var viewModel: ExpenseViewModel
    private var selectedDate: Date = Date()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ExpenseViewModel::class.java]

        setupToolbar()
        setupCategorySpinner()
        setupDatePicker()
        setupSaveButton()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Expense"
    }

    private fun setupCategorySpinner() {
        viewModel.categories.observe(this) { categories ->
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.categorySpinner.adapter = adapter
        }
    }

    private fun setupDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        binding.startTimeEditText.setOnClickListener {
            datePicker.show(supportFragmentManager, "DATE_PICKER")
        }

        datePicker.addOnPositiveButtonClickListener { timestamp ->
            selectedDate = Date(timestamp)
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.startTimeEditText.setText(dateFormat.format(selectedDate))
        }
    }

    private fun setupSaveButton() {
        binding.submitButton.setOnClickListener {
            val amount = binding.amountEditText.text.toString().toDoubleOrNull()
            val category = binding.categorySpinner.selectedItem.toString()
            val description = binding.descriptionEditText.text.toString()

            if (amount == null || amount <= 0) {
                binding.amountEditText.error = "Please enter a valid amount"
                return@setOnClickListener
            }

            val expense = Expense(
                id = UUID.randomUUID().toString(),
                amount = amount,
                category = category,
                date = selectedDate,
                description = description.takeIf { it.isNotBlank() }
            )

            viewModel.addExpense(expense)
            Toast.makeText(this, "Expense added successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 