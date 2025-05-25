package vc.prog3c.poe.ui.views

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import vc.prog3c.poe.R
import vc.prog3c.poe.databinding.ActivityAddIncomeBinding
import vc.prog3c.poe.ui.viewmodels.Income
import vc.prog3c.poe.ui.viewmodels.IncomeViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class AddIncomeView : AppCompatActivity() {
    private lateinit var binding: ActivityAddIncomeBinding
    private lateinit var viewModel: IncomeViewModel
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[IncomeViewModel::class.java]

        setupToolbar()
        setupDatePicker()
        setupSaveButton()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun setupDatePicker() {
        binding.dateInput.setOnClickListener {
            showDatePicker()
        }
        binding.dateInput.setText(dateFormatter.format(calendar.time))
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                binding.dateInput.setText(dateFormatter.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            if (validateInputs()) {
                saveIncome()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

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

        // Validate source
        val source = binding.sourceInput.text.toString()
        if (source.isBlank()) {
            binding.sourceLayout.error = "Source is required"
            isValid = false
        } else {
            binding.sourceLayout.error = null
        }

        // Validate date
        if (binding.dateInput.text.isNullOrBlank()) {
            binding.dateLayout.error = "Date is required"
            isValid = false
        } else {
            binding.dateLayout.error = null
        }

        return isValid
    }

    private fun saveIncome() {
        val amount = binding.amountInput.text.toString().toDouble()
        val source = binding.sourceInput.text.toString()
        val date = calendar.time
        val description = binding.descriptionInput.text.toString().takeIf { it.isNotBlank() }

        val income = Income(
            id = UUID.randomUUID().toString(),
            amount = amount,
            source = source,
            date = date,
            description = description
        )

        viewModel.addIncome(income)
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
                Toast.makeText(this, "Income saved successfully", Toast.LENGTH_SHORT).show()
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