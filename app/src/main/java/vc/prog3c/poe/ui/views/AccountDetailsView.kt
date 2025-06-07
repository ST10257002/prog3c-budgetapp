package vc.prog3c.poe.ui.views

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import vc.prog3c.poe.R
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import vc.prog3c.poe.databinding.ActivityAccountDetailsBinding
import vc.prog3c.poe.ui.viewmodels.AccountDetailsViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import vc.prog3c.poe.core.utils.CurrencyFormatter

class AccountDetailsView : AppCompatActivity(), View.OnClickListener {

    private lateinit var binds: ActivityAccountDetailsBinding
    private lateinit var model: AccountDetailsViewModel


    // --- Lifecycle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBindings()
        setupLayoutUi()
        setupClickListeners()

        // Ensure user is authenticated
        val uid =
            FirebaseAuth.getInstance().currentUser?.uid // TODO: REPLACE WITH SERVICE + THIS SHOULD BE IN VIEWMODEL NOT HERE
        if (uid.isNullOrEmpty()) {
            Toast.makeText(
                this, "User not authenticated", Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }

        model = ViewModelProvider(this)[AccountDetailsViewModel::class.java]

        observeViewModel()

        binds.timePeriodChipGroup.check(R.id.chip1Month) // Default

        // Load details
        val accountId = intent.getStringExtra("account_id")
        if (!accountId.isNullOrEmpty()) {
            model.loadAccountDetails(uid, accountId)
        } else {
            Toast.makeText(
                this, "Account ID not provided", Toast.LENGTH_SHORT
            ).show()
            finish()
        }
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binds.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, DashboardView::class.java))
                    true
                }
                R.id.nav_accounts -> true // you're already on this screen
                R.id.nav_graph -> {
                    startActivity(Intent(this, GraphView::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
        binds.bottomNavigation.selectedItemId = R.id.nav_accounts
    }


    // --- ViewModel


    private fun observeViewModel() {
        model.account.observe(this) { account ->
            account?.let {
                binds.accountNameTextView.text = it.name
                binds.accountTypeTextView.text = it.type

                model.calculatedBalance.observe(this) { balance ->
                    binds.accountBalanceAmount.text = CurrencyFormatter.format(balance)
                }

                val icon = when (it.type.lowercase(Locale.getDefault())) {
                    "credit" -> R.drawable.ic_credit_card
                    "savings" -> R.drawable.ic_savings
                    else -> R.drawable.ic_account_balance
                }
                binds.accountIcon.setImageResource(icon)
                supportActionBar?.title = it.name
            }
        }

        model.transactions.observe(this) { txs ->
            binds.transactionsSummaryTextView.text = "${txs.size} Transactions"
            updateLineChart(txs)
        }

        model.isLoading.observe(this) { /* show loader */ }
        model.error.observe(this) {
            it?.let { msg ->
                Snackbar.make(
                    binds.root, msg, Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }


    // --- Internals


    private fun setupLineChart() {
        binds.accountLineChart.apply {
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


    private fun updateLineChart(transactions: List<Transaction>) {
        if (transactions.isEmpty()) {
            binds.accountLineChart.clear()
            return
        }

        val sorted = transactions.sortedBy { it.date }
        var balance = 0.0
        val entries = sorted.map { tx ->
            balance += when (tx.type) {
                TransactionType.INCOME -> tx.amount
                TransactionType.EXPENSE -> -tx.amount
                else -> 0.0
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

        binds.accountLineChart.data = LineData(ds)
        val labels = sorted.map { fmt ->
            SimpleDateFormat("MMM dd", Locale.getDefault()).format(fmt.date.toDate())
        }
        binds.accountLineChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            labelRotationAngle = -45f
            granularity = TimeUnit.DAYS.toMillis(1).toFloat()
        }
        binds.accountLineChart.invalidate()
    }


    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(this).apply {
            setTitle("Delete Account")
            setMessage("Are you sure you want to delete this account? This action cannot be undone.")
            setPositiveButton("Delete") { _, _ ->
                val accId = model.account.value?.id // <-- Here maybe?
                if (accId != null) model.deleteAccount(accId)
                
                onSupportNavigateUp()
                Toast.makeText( // Go back to previous screen
                    this@AccountDetailsView, "Deletion successful", Toast.LENGTH_SHORT
                ).show()
            }
            setNegativeButton("Cancel", null)
        }.show()
    }


    // --- Event Handlers


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        if (isTaskRoot) {
            // If there's nothing to go back to, launch Dashboard
            startActivity(Intent(this, DashboardView::class.java))
            finish()
        } else {
            super.onBackPressed()
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            binds.viewTransactionsButton.id -> {
                val intent = Intent(this, TransactionsActivity::class.java).apply {
                    putExtra("account_id", model.account.value?.id)
                }
                startActivity(intent)
            }

            binds.deleteAccountButton.id -> {
                showDeleteConfirmationDialog()
            }
        }
    }


    private fun setupClickListeners() {
        binds.viewTransactionsButton.setOnClickListener(this)
        binds.deleteAccountButton.setOnClickListener(this)
    }


    // --- UI Configuration


    private fun setupToolbar() {
        setSupportActionBar(binds.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Account"
    }


    private fun setupTimePeriodChips() {
        binds.timePeriodChipGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.chip1Week -> model.filterTransactionsByTimePeriod("1 week")
                R.id.chip1Month -> model.filterTransactionsByTimePeriod("1 month")
                R.id.chip3Months -> model.filterTransactionsByTimePeriod("3 months")
            }
        }
    }


    // --- UI


    private fun setupBindings() {
        binds = ActivityAccountDetailsBinding.inflate(layoutInflater)
    }


    private fun setupLayoutUi() {
        setContentView(binds.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binds.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupStatusBar()
        setupToolbar()
        setupLineChart()
        setupTimePeriodChips()
    }

    private fun setupStatusBar() {
        window.statusBarColor = getColor(R.color.primary)
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and 
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
    }

}
