package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import vc.prog3c.poe.R
import vc.prog3c.poe.databinding.ActivityTransactionsBinding
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import vc.prog3c.poe.ui.adapters.TransactionAdapter
import vc.prog3c.poe.ui.viewmodels.TransactionViewModel
import java.text.NumberFormat
import java.util.Locale

class TransactionsView : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionsBinding
    private lateinit var viewModel: TransactionViewModel
    private lateinit var adapter: TransactionAdapter
    private var accountId: String? = null
    private var currentType: TransactionType? = null // Track current filter

    private val addTransactionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // Instead of extracting the transaction, just reload from database
            viewModel.loadTransactions(accountId)
            Toast.makeText(this, "Transaction added", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[TransactionViewModel::class.java]
        accountId = intent.getStringExtra("account_id")

        setupToolbar()
        setupRecyclerView()
        setupFilterChips()
        setupSwipeRefresh()
        setupAddTransactionButton()
        observeViewModel()

        // Initial load
        viewModel.loadTransactions(accountId)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = when (currentType) {
            TransactionType.INCOME -> "Income"
            TransactionType.EXPENSE -> "Expenses"
            else -> "Transactions"
        }
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter()
        binding.transactionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@TransactionsView)
            adapter = this@TransactionsView.adapter
            layoutAnimation = AnimationUtils.loadLayoutAnimation(
                context, R.anim.layout_animation_fall_down
            )
        }
    }

    private fun setupFilterChips() {
        binding.filterChipGroup.setOnCheckedChangeListener { group, checkedId ->
            currentType = when (group.findViewById<Chip>(checkedId)?.id) {
                R.id.allChip -> {
                    viewModel.loadTransactions(accountId)
                    null
                }
                R.id.incomeChip -> {
                    viewModel.filterTransactionsByType(TransactionType.INCOME)
                    TransactionType.INCOME
                }
                R.id.expenseChip -> {
                    viewModel.filterTransactionsByType(TransactionType.EXPENSE)
                    TransactionType.EXPENSE
                }
                else -> null
            }
            setupToolbar() // Update title
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.apply {
            setColorSchemeResources(R.color.primary, R.color.green, R.color.red)
            setOnRefreshListener { viewModel.loadTransactions(accountId) }
        }
    }

    private fun setupAddTransactionButton() {
        binding.addTransactionButton.setOnClickListener {
            if (accountId == null) {
                Toast.makeText(this, "Account ID missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = if (currentType == TransactionType.INCOME) {
                Intent(this, AddIncomeView::class.java)
            } else {
                Intent(this, AddExpenseView::class.java)
            }
            intent.putExtra("account_id", accountId)
            addTransactionLauncher.launch(intent)
        }
    }

    private fun observeViewModel() {
        viewModel.transactions.observe(this) { list ->
            adapter.submitList(list)
            binding.transactionsRecyclerView.scheduleLayoutAnimation()
        }
        viewModel.totalIncome.observe(this) { income ->
            binding.moneyInTextView.text = formatCurrency(income)
        }
        viewModel.totalExpenses.observe(this) { expense ->
            binding.moneyOutTextView.text = formatCurrency(expense)
        }
        viewModel.isLoading.observe(this) { loading ->
            binding.swipeRefreshLayout.isRefreshing = loading
            binding.loadingProgressBar.visibility =
                if (loading) View.VISIBLE else View.GONE
        }
        viewModel.error.observe(this) { msg ->
            msg?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
                    .setAction("Retry") { viewModel.loadTransactions(accountId) }
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
}