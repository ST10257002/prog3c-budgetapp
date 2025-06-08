package vc.prog3c.poe.ui.views

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import vc.prog3c.poe.R
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import vc.prog3c.poe.data.models.SortOption
import vc.prog3c.poe.databinding.ViewTransactionsBinding
import vc.prog3c.poe.ui.adapters.TransactionAdapter
import vc.prog3c.poe.ui.viewmodels.AchievementViewModel
import vc.prog3c.poe.ui.viewmodels.TransactionState
import vc.prog3c.poe.ui.viewmodels.TransactionViewModel
import java.text.NumberFormat
import java.util.Locale

class TransactionsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ViewTransactionsBinding.inflate(LayoutInflater.from(context), this, true)
    private lateinit var viewModel: TransactionViewModel
    private lateinit var transactionAdapter: TransactionAdapter
    private var onAddTransactionClickListener: (() -> Unit)? = null
    private var currentAccountId: String? = null

    init {
        setupRecyclerView()
        setupButtons()
        setupSwipeRefresh()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(
            onItemClick = { transaction ->
                val intent = Intent(context, TransactionDetailsActivity::class.java).apply {
                    putExtra(TransactionDetailsActivity.EXTRA_TRANSACTION_ID, transaction.id)
                    currentAccountId?.let { putExtra("account_id", it) }
                }
                context.startActivity(intent)
            },
            onItemLongClick = { transaction ->
                AlertDialog.Builder(context)
                    .setTitle("Delete Transaction")
                    .setMessage("Are you sure you want to delete this transaction?")
                    .setPositiveButton("Delete") { _, _ ->
                        currentAccountId?.let { accountId ->
                            viewModel.deleteTransaction(transaction.id, accountId)
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        binding.transactionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionAdapter
        }
    }

    private fun setupButtons() {
        binding.btnAddTransaction.setOnClickListener {
            onAddTransactionClickListener?.invoke()
        }

        binding.btnFilter.setOnClickListener {
            showFilterDialog()
        }

        binding.btnSort.setOnClickListener {
            showSortDialog()
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.apply {
            setColorSchemeResources(R.color.primary, R.color.green, R.color.red)
            setOnRefreshListener {
                currentAccountId?.let { viewModel.loadTransactions(it) }
            }
        }
    }

    private fun showFilterDialog() {
        val options = arrayOf("All", "Income", "Expense")
        AlertDialog.Builder(context)
            .setTitle("Filter Transactions")
            .setItems(options) { _, which ->
                val selectedFilter = options[which]
                viewModel.filterTransactions(selectedFilter)
            }
            .show()
    }

    private fun showSortDialog() {
        val options = arrayOf("Date (Newest)", "Date (Oldest)", "Amount (High to Low)", "Amount (Low to High)")
        AlertDialog.Builder(context)
            .setTitle("Sort Transactions")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> viewModel.sortTransactions(SortOption.DATE_DESC)
                    1 -> viewModel.sortTransactions(SortOption.DATE_ASC)
                    2 -> viewModel.sortTransactions(SortOption.AMOUNT_DESC)
                    3 -> viewModel.sortTransactions(SortOption.AMOUNT_ASC)
                }
            }
            .show()
    }

    fun setViewModel(
        viewModel: TransactionViewModel,
        achievementViewModel: AchievementViewModel,
        context: Context,
        accountId: String
    ) {
        this.viewModel = viewModel
        this.currentAccountId = accountId
        this.viewModel.achievementViewModel = achievementViewModel // 🔥 Injected safely here

        viewModel.transactions.observe(context as LifecycleOwner) { transactions ->
            transactionAdapter.submitList(transactions)
            updateTotals(transactions)
            binding.swipeRefreshLayout.isRefreshing = false
        }

        viewModel.transactionState.observe(context) { state ->
            when (state) {
                is TransactionState.Loading -> {
                    binding.swipeRefreshLayout.isRefreshing = true
                }
                is TransactionState.Success -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                }
                is TransactionState.Error -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }

        viewModel.loadTransactions(accountId)
    }

    fun setOnAddTransactionClickListener(listener: () -> Unit) {
        onAddTransactionClickListener = listener
    }

    private fun updateTotals(transactions: List<Transaction>) {
        val moneyIn = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val moneyOut = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        binding.moneyInTextView.text = formatter.format(moneyIn)
        binding.moneyOutTextView.text = formatter.format(moneyOut)
    }

    companion object {
        private const val TAG = "TransactionsView"
    }
}
