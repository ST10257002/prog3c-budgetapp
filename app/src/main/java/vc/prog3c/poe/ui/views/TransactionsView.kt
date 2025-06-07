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
import vc.prog3c.poe.ui.viewmodels.TransactionState
import java.text.NumberFormat
import java.util.Locale
import android.widget.EditText
import android.app.DatePickerDialog
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Date
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.databinding.ViewTransactionsBinding
import android.widget.ArrayAdapter

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
        transactionAdapter = TransactionAdapter { transaction ->
            val intent = Intent(context, TransactionDetailsActivity::class.java).apply {
                putExtra(TransactionDetailsActivity.EXTRA_TRANSACTION_ID, transaction.id)
                currentAccountId?.let { putExtra("account_id", it) }
            }
            context.startActivity(intent)
        }
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
        android.app.AlertDialog.Builder(context)
            .setTitle("Filter Transactions")
            .setItems(options) { _, which ->
                val selectedFilter = options[which]
                viewModel.filterTransactions(selectedFilter)
            }
            .show()
    }

    private fun showSortDialog() {
        val options = arrayOf("Date (Newest)", "Date (Oldest)", "Amount (High to Low)", "Amount (Low to High)")
        android.app.AlertDialog.Builder(context)
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

    fun setViewModel(viewModel: TransactionViewModel, context: Context, accountId: String) {
        this.viewModel = viewModel
        this.currentAccountId = accountId
        viewModel.transactions.observe(context as androidx.lifecycle.LifecycleOwner) { transactions ->
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