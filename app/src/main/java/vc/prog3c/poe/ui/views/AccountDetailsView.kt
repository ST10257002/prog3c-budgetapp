package vc.prog3c.poe.ui.views

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import vc.prog3c.poe.R
import vc.prog3c.poe.data.models.Account
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import vc.prog3c.poe.databinding.ActivityAccountDetailsBinding
import vc.prog3c.poe.ui.adapters.AccountAdapter
import vc.prog3c.poe.ui.viewmodels.AccountDetailsViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

class AccountDetailsView : AppCompatActivity() {

    private lateinit var binding: ActivityAccountDetailsBinding
    private lateinit var viewModel: AccountDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AccountDetailsViewModel::class.java]

        setupToolbar()
        setupTimePeriodChips()
        setupButtons()
        setupLineChart()
        observeViewModel()

        val accountId = intent.getStringExtra("account_id")
        if (accountId != null) {
            viewModel.loadAccountDetails(accountId)
        } else {
            // Handle case where accountId is not passed
            Toast.makeText(this, "Account ID not provided", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Account"
    }

    private fun setupTimePeriodChips() {
        binding.timePeriodChipGroup.setOnCheckedChangeListener { group, checkedId ->
            val chip = group.findViewById<com.google.android.material.chip.Chip>(checkedId)
            when (chip?.id) {
                R.id.chip1Week -> viewModel.filterTransactionsByTimePeriod("1 week")
                R.id.chip1Month -> viewModel.filterTransactionsByTimePeriod("1 month")
                R.id.chip3Months -> viewModel.filterTransactionsByTimePeriod("3 months")
            }
        }
    }

    private fun setupButtons() {
        binding.viewTransactionsButton.setOnClickListener { 
            val intent = Intent(this, TransactionsView::class.java)
            intent.putExtra("account_id", viewModel.account.value?.id)
            startActivity(intent)
        }

        binding.deleteAccountButton.setOnClickListener { 
            showDeleteConfirmationDialog()
        }
    }

    private fun setupLineChart() {
        binding.accountLineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setDrawGridBackground(false)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)

            // Configure X-axis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.setDrawAxisLine(true)
            xAxis.textColor = Color.BLACK
            xAxis.textSize = 10f
            xAxis.setAvoidFirstLastClipping(true)

            // Configure Y-axis (left)
            axisLeft.setDrawGridLines(true)
            axisLeft.setDrawAxisLine(true)
            axisLeft.textColor = Color.BLACK
            axisLeft.textSize = 10f

            // Configure Y-axis (right) - disable or customize if needed
            axisRight.isEnabled = false // Disable right Y-axis

            legend.isEnabled = false
            animateX(1000)

            // Improve appearance
            extraBottomOffset = 10f // Add some offset to prevent labels from being cut off
            setExtraOffsets(10f, 10f, 10f, 10f) // Add extra space around the chart
        }
    }

    private fun observeViewModel() {
        viewModel.account.observe(this) { account ->
            account?.let {
                binding.accountNameTextView.text = it.name
                binding.accountTypeTextView.text = it.type
                binding.accountBalanceAmount.text = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(it.balance)

                val iconResId = when (it.type.lowercase()) {
                    "credit" -> R.drawable.ic_credit_card
                    "savings" -> R.drawable.ic_savings
                    else -> R.drawable.ic_account_balance
                }
                binding.accountIcon.setImageResource(iconResId)

                // Update toolbar title with account name
                supportActionBar?.title = it.name
            }
        }

        viewModel.transactions.observe(this) { transactions ->
            binding.transactionsSummaryTextView.text = "${transactions.size} Transactions"
            updateLineChart(transactions)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            // TODO: Show/hide loading indicator
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                showError(it)
            }
        }
    }

    private fun updateLineChart(transactions: List<Transaction>) {
        if (transactions.isEmpty()) {
            binding.accountLineChart.data = null
            binding.accountLineChart.invalidate()
            return
        }

        // Sort transactions by date to plot correctly on the graph
        val sortedTransactions = transactions.sortedBy { it.date }

        // Calculate running balance over time
        var runningBalance = 0.0
        val entries = sortedTransactions.mapIndexed { index, transaction ->
            when (transaction.type) {
                TransactionType.INCOME -> runningBalance += transaction.amount
                TransactionType.EXPENSE -> runningBalance -= transaction.amount
                TransactionType.ALL -> { /* Should not happen with individual transactions */ }
            }
            // Use date in milliseconds as x-value for chronological order
            Entry(transaction.date.time.toFloat(), runningBalance.toFloat())
        }

        val dataSet = LineDataSet(entries, "Balance").apply {
            color = resources.getColor(R.color.primary, null)
            valueTextColor = Color.BLACK
            setCircleColor(resources.getColor(R.color.primary, null))
            setDrawValues(false) // Hide individual data point values
            setDrawFilled(true) // Fill the area below the line
            fillColor = resources.getColor(R.color.primary, null)
            fillAlpha = 85
            mode = LineDataSet.Mode.CUBIC_BEZIER // Smooth the line
            lineWidth = 2f // Thicker line
        }

        val lineData = LineData(dataSet)
        binding.accountLineChart.data = lineData

        // Format X-axis to show dates
        val xAxis = binding.accountLineChart.xAxis
        xAxis.valueFormatter = object : IndexAxisValueFormatter() {
            private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            override fun getFormattedValue(value: Float): String {
                return dateFormat.format(Date(value.toLong()))
            }
        }
        xAxis.setLabelCount(entries.size, true) // Show all date labels (can be adjusted)
        xAxis.granularity = TimeUnit.DAYS.toMillis(1).toFloat() // Ensure labels are one day apart
        xAxis.labelRotationAngle = -45f // Rotate labels if they overlap

        binding.accountLineChart.invalidate()
        binding.accountLineChart.animateX(1000)
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete this account? This action cannot be undone.")
            .setPositiveButton("Delete") { dialog, _ ->
                viewModel.deleteAccount()
                dialog.dismiss()
                // TODO: Navigate back to Accounts view after successful deletion
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 