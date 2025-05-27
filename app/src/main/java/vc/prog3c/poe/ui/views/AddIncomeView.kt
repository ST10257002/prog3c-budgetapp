package vc.prog3c.poe.ui.views

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import vc.prog3c.poe.R
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import vc.prog3c.poe.databinding.ActivityAddIncomeBinding
import vc.prog3c.poe.ui.viewmodels.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import com.google.firebase.Timestamp

class AddIncomeView : AppCompatActivity() {
    private lateinit var binding: ActivityAddIncomeBinding
    private lateinit var viewModel: TransactionViewModel
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var accountId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Use TransactionViewModel
        viewModel = ViewModelProvider(this)[TransactionViewModel::class.java]
        accountId = intent.getStringExtra("account_id")

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

        val source = binding.sourceInput.text.toString()
        if (source.isBlank()) {
            binding.sourceLayout.error = "Source is required"
            isValid = false
        } else {
            binding.sourceLayout.error = null
        }

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
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val account = accountId ?: return

        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            userId = userId,
            accountId = account,
            type = TransactionType.INCOME,
            amount = amount,
            category = source,
            date = Timestamp(date),
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
