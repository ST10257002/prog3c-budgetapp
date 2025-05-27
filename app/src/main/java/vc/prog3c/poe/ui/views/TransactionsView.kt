package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import vc.prog3c.poe.R
import vc.prog3c.poe.databinding.ActivityTransactionsBinding
import vc.prog3c.poe.data.models.TransactionType
import vc.prog3c.poe.ui.adapters.TransactionAdapter
import vc.prog3c.poe.ui.viewmodels.TransactionViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionsView : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionsBinding
    private lateinit var viewModel: TransactionViewModel
    private lateinit var adapter: TransactionAdapter

    private var accountId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[TransactionViewModel::class.java]

        accountId = intent.getStringExtra("account_id")

        setupToolbar()
        setupBottomNavigation()
        setupRecyclerView()
        setupFilterChips()
        setupSwipeRefresh()
        setupAddTransactionButton()
        observeViewModel()

        viewModel.loadTransactions(accountId)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Transactions"
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, DashboardView::class.java))
                    finish()
                    true
                }
                R.id.nav_accounts -> {
                    startActivity(Intent(this, AccountsView::class.java))
                    finish()
                    true
                }
                R.id.nav_graph -> {
                    startActivity(Intent(this, GraphView::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
        // Removed nav_transactions as it's no longer in the menu
        // binding.bottomNavigation.selectedItemId = R.id.nav_transactions
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter()
        binding.transactionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@TransactionsView)
            adapter = this@TransactionsView.adapter
            layoutAnimation = AnimationUtils.loadLayoutAnimation(
                context,
                R.anim.layout_animation_fall_down
            )
        }
    }

    private fun setupFilterChips() {
        binding.filterChipGroup.setOnCheckedChangeListener { group, checkedId ->
            val chip = group.findViewById<Chip>(checkedId)
            val transactionType = when (chip?.id) {
                R.id.allChip -> TransactionType.ALL
                R.id.incomeChip -> TransactionType.INCOME
                R.id.expenseChip -> TransactionType.EXPENSE
                else -> TransactionType.ALL
            }
            viewModel.filterTransactionsByType(transactionType, accountId)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.apply {
            setColorSchemeResources(
                R.color.primary,
                R.color.green,
                R.color.red
            )
            setOnRefreshListener {
                refreshData()
            }
        }
    }

    private fun refreshData() {
        viewModel.refreshTransactions(accountId)
    }

    private fun setupAddTransactionButton() {
        binding.addTransactionButton.setOnClickListener {
            showTransactionTypeDialog()
        }
    }

    private fun showTransactionTypeDialog() {
        val options = arrayOf("Income", "Expense")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Add Transaction")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> startAddIncome()
                    1 -> startAddExpense()
                }
            }
            .show()
    }

    private fun startAddIncome() {
        val intent = Intent(this, AddIncomeView::class.java)
        accountId?.let { intent.putExtra("account_id", it) }
        startActivity(intent)
    }

    private fun startAddExpense() {
        val intent = Intent(this, AddExpenseView::class.java)
        accountId?.let { intent.putExtra("account_id", it) }
        startActivity(intent)
    }

    private fun observeViewModel() {
        viewModel.transactions.observe(this) { transactions ->
            adapter.submitList(transactions)
            binding.transactionsRecyclerView.scheduleLayoutAnimation()
        }

        viewModel.totalIncome.observe(this) { income ->
            binding.moneyInTextView.text = formatCurrency(income)
        }

        viewModel.totalExpenses.observe(this) { expenses ->
            binding.moneyOutTextView.text = formatCurrency(expenses)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
            binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                showError(it)
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Retry") {
                refreshData()
            }
            .show()
    }

    private fun formatCurrency(amount: Double): String {
        return NumberFormat.getCurrencyInstance(Locale.getDefault()).format(amount)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 