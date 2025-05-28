package vc.prog3c.poe.ui.views

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart
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
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class AccountDetailsView : AppCompatActivity() {

    private lateinit var binding: ActivityAccountDetailsBinding
    private lateinit var viewModel: AccountDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ensure user is authenticated
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel = ViewModelProvider(this)[AccountDetailsViewModel::class.java]

        setupToolbar()
        setupTimePeriodChips()
        setupButtons()
        setupLineChart()
        observeViewModel()

        // Default to 1 month
        binding.timePeriodChipGroup.check(R.id.chip1Month)

        // Load details
        val accountId = intent.getStringExtra("account_id")
        if (!accountId.isNullOrEmpty()) {
            viewModel.loadAccountDetails(uid, accountId)
        } else {
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
                R.id.chip1Week  -> viewModel.filterTransactionsByTimePeriod("1 week")
                R.id.chip1Month -> viewModel.filterTransactionsByTimePeriod("1 month")
                R.id.chip3Months-> viewModel.filterTransactionsByTimePeriod("3 months")
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

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                setDrawAxisLine(true)
                textColor = Color.BLACK
                textSize = 10f
                setAvoidFirstLastClipping(true)
            }

            axisLeft.apply {
                setDrawGridLines(true)
                setDrawAxisLine(true)
                textColor = Color.BLACK
                textSize = 10f
            }

            axisRight.isEnabled = false
            legend.isEnabled = false
            animateX(1000)
            extraBottomOffset = 10f
            setExtraOffsets(10f, 10f, 10f, 10f)
        }
    }

    private fun observeViewModel() {
        viewModel.account.observe(this) { account ->
            account?.let {
                binding.accountNameTextView.text = it.name
                binding.accountTypeTextView.text = it.type

                viewModel.calculatedBalance.observe(this) { balance ->
                    val za = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
                    binding.accountBalanceAmount.text = za.format(balance)
                }


                val icon = when (it.type.lowercase(Locale.getDefault())) {
                    "credit" -> R.drawable.ic_credit_card
                    "savings"-> R.drawable.ic_savings
                    else      -> R.drawable.ic_account_balance
                }
                binding.accountIcon.setImageResource(icon)
                supportActionBar?.title = it.name
            }
        }

        viewModel.transactions.observe(this) { txs ->
            binding.transactionsSummaryTextView.text = "${txs.size} Transactions"
            updateLineChart(txs)
        }

        viewModel.isLoading.observe(this) { /* show loader */ }
        viewModel.error.observe(this) { it?.let { msg -> showError(msg) } }
    }

    private fun updateLineChart(transactions: List<Transaction>) {
        if (transactions.isEmpty()) {
            binding.accountLineChart.clear()
            return
        }

        val sorted = transactions.sortedBy { it.date }
        var balance = 0.0
        val entries = sorted.map { tx ->
            balance += when (tx.type) {
                TransactionType.INCOME  -> tx.amount
                TransactionType.EXPENSE -> -tx.amount
                else                    -> 0.0
            }
            Entry(tx.date.toDate().time.toFloat(), balance.toFloat())
        }

        val ds = LineDataSet(entries, "Balance").apply {
            setDrawValues(false)
            setDrawFilled(true)
            fillAlpha = 85
            mode = LineDataSet.Mode.CUBIC_BEZIER
            lineWidth = 2f
            val col = resources.getColor(R.color.primary, null)
            color = col
            setCircleColor(col)
            fillColor = col
            valueTextColor = Color.BLACK
        }

        binding.accountLineChart.data = LineData(ds)
        val labels = sorted.map { fmt ->
            SimpleDateFormat("MMM dd", Locale.getDefault()).format(fmt.date.toDate())
        }
        binding.accountLineChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            labelRotationAngle = -45f
            granularity = TimeUnit.DAYS.toMillis(1).toFloat()
        }
        binding.accountLineChart.invalidate()
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete this account? This action cannot be undone.")
            .setPositiveButton("Delete") { d, _ ->
                val accId = viewModel.account.value?.id
                if (accId != null) viewModel.deleteAccount(accId)
                d.dismiss()
                finish()
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
