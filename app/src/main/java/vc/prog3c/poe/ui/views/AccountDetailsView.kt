// ✅ AccountDetailsView.kt
package vc.prog3c.poe.ui.views

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import vc.prog3c.poe.R
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import vc.prog3c.poe.databinding.ActivityAccountDetailsBinding
import vc.prog3c.poe.ui.viewmodels.AccountDetailsViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AccountDetailsView : AppCompatActivity() {
    private lateinit var binding: ActivityAccountDetailsBinding
    private lateinit var viewModel: AccountDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        viewModel = ViewModelProvider(this)[AccountDetailsViewModel::class.java]

        setupToolbar()
        setupTimePeriodChips()
        setupButtons()
        setupLineChart()
        observeViewModel()

        binding.timePeriodChipGroup.check(R.id.chip1Month)
        intent.getStringExtra("account_id")?.let {
            viewModel.loadAccountDetails(it) // Updated: only pass accountId as required
        } ?: run {
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
        binding.timePeriodChipGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.chip1Week -> viewModel.filterTransactionsByTimePeriod("1 week")
                R.id.chip1Month -> viewModel.filterTransactionsByTimePeriod("1 month")
                R.id.chip3Months -> viewModel.filterTransactionsByTimePeriod("3 months")
            }
        }
    }

    private fun setupButtons() {
        binding.viewTransactionsButton.setOnClickListener {
            Intent(this, TransactionsView::class.java).apply {
                putExtra("account_id", viewModel.account.value?.id)
                startActivity(this)
            }
        }
        binding.deleteAccountButton.setOnClickListener { showDeleteConfirmationDialog() }
    }

    private fun setupLineChart() {
        binding.accountLineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textColor = Color.BLACK
            axisLeft.textColor = Color.BLACK
            axisRight.isEnabled = false
            legend.isEnabled = false
            animateX(1000)
        }
    }

    private fun observeViewModel() {
        viewModel.account.observe(this) { account ->
            binding.accountNameTextView.text = account?.name
            binding.accountTypeTextView.text = account?.type
            binding.accountIcon.setImageResource(
                when (account?.type?.lowercase(Locale.getDefault())) {
                    "credit" -> R.drawable.ic_credit_card
                    "savings" -> R.drawable.ic_savings
                    else -> R.drawable.ic_account_balance
                }
            )
            viewModel.calculatedBalance.observe(this) { bal ->
                binding.accountBalanceAmount.text = NumberFormat.getCurrencyInstance(Locale("en", "ZA")).format(bal)
            }
        }

        viewModel.transactions.observe(this) {
            binding.transactionsSummaryTextView.text = "${it.size} Transactions"
            updateLineChart(it)
        }

        viewModel.error.observe(this) { it?.let { showError(it) } }
    }

    private fun updateLineChart(transactions: List<Transaction>) {
        if (transactions.isEmpty()) return binding.accountLineChart.clear()
        val entries = mutableListOf<Entry>()
        var runningBalance = 0.0
        val labels = transactions.sortedBy { it.date }.mapIndexed { idx, tx ->
            runningBalance += when (tx.type) {
                TransactionType.INCOME -> tx.amount
                TransactionType.EXPENSE -> -tx.amount
            }
            entries.add(Entry(idx.toFloat(), runningBalance.toFloat()))
            SimpleDateFormat("MMM dd", Locale.getDefault()).format(tx.date.toDate())
        }

        LineDataSet(entries, "Balance").apply {
            valueTextColor = Color.BLACK
            setDrawFilled(true)
            fillAlpha = 85
            color = resources.getColor(R.color.primary, null)
            fillColor = resources.getColor(R.color.primary, null)
            binding.accountLineChart.data = LineData(this)
            binding.accountLineChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            binding.accountLineChart.invalidate()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete this account? This action cannot be undone.")
            .setPositiveButton("Delete") { d, _ ->
                viewModel.account.value?.id?.let { viewModel.deleteAccount(it) }
                d.dismiss(); finish()
            }
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
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