package vc.prog3c.poe.ui.views

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.datepicker.MaterialDatePicker
import vc.prog3c.poe.databinding.ActivityAddIncomeBinding
import vc.prog3c.poe.ui.viewmodels.Income
import vc.prog3c.poe.ui.viewmodels.IncomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AddIncomeView : AppCompatActivity() {
    private lateinit var binding: ActivityAddIncomeBinding
    private lateinit var viewModel: IncomeViewModel
    private var selectedDate: Date = Date()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[IncomeViewModel::class.java]

        setupToolbar()
        setupDatePicker()
        setupSaveButton()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Income"
    }

    private fun setupDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        binding.dateInput.setOnClickListener {
            datePicker.show(supportFragmentManager, "DATE_PICKER")
        }

        datePicker.addOnPositiveButtonClickListener { timestamp ->
            selectedDate = Date(timestamp)
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.dateInput.setText(dateFormat.format(selectedDate))
        }
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            val amount = binding.amountInput.text.toString().toDoubleOrNull()
            val source = binding.sourceInput.text.toString()
            val description = binding.descriptionInput.text.toString()

            if (amount == null || amount <= 0) {
                binding.amountInput.error = "Please enter a valid amount"
                return@setOnClickListener
            }

            if (source.isBlank()) {
                binding.sourceInput.error = "Please enter income source"
                return@setOnClickListener
            }

            val income = Income(
                id = UUID.randomUUID().toString(),
                amount = amount,
                source = source,
                date = selectedDate,
                description = description.takeIf { it.isNotBlank() }
            )

            viewModel.addIncome(income)
            Toast.makeText(this, "Income added successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 