// Updated Transaction Path continues in View layer

package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import vc.prog3c.poe.utils.TransactionState

class TransactionsView : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionsBinding
    private lateinit var viewModel: TransactionViewModel
    private lateinit var adapter: TransactionAdapter
    private var accountId: String? = null
    private var currentType: TransactionType? = null

    private val addTransactionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.loadTransactions(accountId ?: return@registerForActivityResult)
            Toast.makeText(this, "Transaction added", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        accountId = intent.getStringExtra("account_id")
        if (accountId == null) {
            Toast.makeText(this, "Account ID missing", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        viewModel = ViewModelProvider(this)[TransactionViewModel::class.java]

        setupRecyclerView()
        setupToolbar()
        setupChips()
        setupSwipeRefresh()
        setupObservers()
        setupAddButton()

        viewModel.loadTransactions(accountId!!)
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter()
        binding.transactionsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.transactionsRecyclerView.adapter = adapter
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Transactions"
    }

    private fun setupChips() {
        binding.filterChipGroup.setOnCheckedChangeListener { group, checkedId ->
            val type = when (checkedId) {
                R.id.incomeChip -> TransactionType.INCOME
                R.id.expenseChip -> TransactionType.EXPENSE
                else -> null
            }
            currentType = type
            viewModel.loadTransactions(accountId!!, type)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadTransactions(accountId!!, currentType)
        }
    }

    private fun setupObservers() {
        viewModel.transactions.observe(this) { adapter.submitList(it) }
        viewModel.totalIncome.observe(this) { binding.moneyInTextView.text = formatCurrency(it) }
        viewModel.totalExpenses.observe(this) { binding.moneyOutTextView.text = formatCurrency(it) }
        viewModel.state.observe(this) {
            binding.swipeRefreshLayout.isRefreshing = it is TransactionState.Loading
            when (it) {
                is TransactionState.Error -> showSnackbar(it.message)
                else -> Unit
            }
        }
    }

    private fun setupAddButton() {
        binding.addTransactionButton.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java).apply {
                putExtra("account_id", accountId)
            }
            addTransactionLauncher.launch(intent)
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun formatCurrency(value: Double): String =
        java.text.NumberFormat.getCurrencyInstance().format(value)

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
