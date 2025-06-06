package vc.prog3c.poe.ui.views

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import vc.prog3c.poe.R
import vc.prog3c.poe.data.models.FilterOption
import vc.prog3c.poe.data.models.SortOption
import vc.prog3c.poe.databinding.ActivityTransactionsBinding
import vc.prog3c.poe.data.models.TransactionType
import vc.prog3c.poe.ui.adapters.TransactionAdapter
import vc.prog3c.poe.ui.viewmodels.TransactionViewModel
import java.text.NumberFormat
import java.util.Locale

class TransactionsView : AppCompatActivity() {
    private lateinit var binds: ActivityTransactionsBinding
    private lateinit var model: TransactionViewModel
    private lateinit var adapter: TransactionAdapter
    private var accountId: String? = null
    private var currentType: TransactionType? = null // Track current filter

    private val addTransactionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // Instead of extracting the transaction, just reload from database
            model.loadTransactions(accountId)
            Toast.makeText(this, "Transaction added", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binds = ActivityTransactionsBinding.inflate(layoutInflater)
        setContentView(binds.root)
        ViewCompat.setOnApplyWindowInsetsListener(binds.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        model = ViewModelProvider(this)[TransactionViewModel::class.java]
        accountId = intent.getStringExtra("account_id")

        setupToolbar()
        setupRecyclerView()
        //setupFilterChips()
        setupFilterButton()
        setupSwipeRefresh()
        setupAddTransactionButton()
        observeViewModel()
        setupBottomNavigation()


        // Initial load
        model.loadTransactions(accountId)
    }

    private fun setupToolbar() {
        setSupportActionBar(binds.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = when (currentType) {
            TransactionType.INCOME -> "Income"
            TransactionType.EXPENSE -> "Expenses"
            else -> "Transactions"
        }
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter()
        binds.transactionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@TransactionsView)
            adapter = this@TransactionsView.adapter
            layoutAnimation = AnimationUtils.loadLayoutAnimation(
                context, R.anim.layout_animation_fall_down
            )
        }
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


    /*
        private fun setupFilterChips() {
            binds.filterChipGroup.setOnCheckedChangeListener { group, checkedId ->
                currentType = when (group.findViewById<Chip>(checkedId)?.id) {
                    R.id.allChip -> {
                        model.loadTransactions(accountId)
                        null
                    }
                    R.id.incomeChip -> {
                        model.filterTransactionsByType(TransactionType.INCOME)
                        TransactionType.INCOME
                    }
                    R.id.expenseChip -> {
                        model.filterTransactionsByType(TransactionType.EXPENSE)
                        TransactionType.EXPENSE
                    }
                    else -> null
                }
                setupToolbar() // Update title
            }
        }
    */

    private fun setupSwipeRefresh() {
        binds.swipeRefreshLayout.apply {
            setColorSchemeResources(R.color.primary, R.color.green, R.color.red)
            setOnRefreshListener { model.loadTransactions(accountId) }
        }
    }

    private fun setupAddTransactionButton() {
        binds.addTransactionButton.setOnClickListener {
            if (accountId == null) {
                Toast.makeText(this, "Account ID missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, AddTransactionActivity::class.java).apply {
                putExtra("account_id", accountId)
            }
            addTransactionLauncher.launch(intent)
        }
    }

    private fun observeViewModel() {
        model.transactions.observe(this) { list ->
            adapter.submitList(list)
            binds.transactionsRecyclerView.scheduleLayoutAnimation()
        }
        model.totalIncome.observe(this) { income ->
            binds.moneyInTextView.text = formatCurrency(income)
        }
        model.totalExpenses.observe(this) { expense ->
            binds.moneyOutTextView.text = formatCurrency(expense)
        }
        model.isLoading.observe(this) { loading ->
            binds.swipeRefreshLayout.isRefreshing = loading
            binds.loadingProgressBar.visibility =
                if (loading) View.VISIBLE else View.GONE
        }
        model.error.observe(this) { msg ->
            msg?.let {
                Snackbar.make(binds.root, it, Snackbar.LENGTH_LONG)
                    .setAction("Retry") { model.loadTransactions(accountId) }
                    .show()
            }
        }
    }

    private fun formatCurrency(amount: Double): String =
        NumberFormat.getCurrencyInstance(Locale.getDefault()).format(amount)

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setupFilterButton() {
        val filterIcon = findViewById<ImageView>(R.id.filterIcon)
        filterIcon.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_transaction_filter, null)

            val filterGroup = dialogView.findViewById<RadioGroup>(R.id.filterRadioGroup)
            val sortGroup = dialogView.findViewById<RadioGroup>(R.id.sortRadioGroup)

            val dialog = AlertDialog.Builder(this)
                .setTitle("Filter & Sort")
                .setView(dialogView)
                .setPositiveButton("Apply") { _, _ ->
                    val filter = when (filterGroup.checkedRadioButtonId) {
                        R.id.filterIncome -> FilterOption.INCOME
                        R.id.filterExpense -> FilterOption.EXPENSE
                        else -> FilterOption.ALL
                    }
                    val sort = when (sortGroup.checkedRadioButtonId) {
                        R.id.sortOldest -> SortOption.OLDEST
                        R.id.sortHighest -> SortOption.HIGHEST
                        R.id.sortLowest -> SortOption.LOWEST
                        else -> SortOption.NEWEST
                    }
                    model.applyFilterAndSort(filter, sort)
                }
                .setNeutralButton("Reset") { _, _ ->
                    model.applyFilterAndSort(FilterOption.ALL, SortOption.NEWEST)
                }
                .setNegativeButton("Cancel", null)

                .create()

            dialog.show()
        }
    }
}