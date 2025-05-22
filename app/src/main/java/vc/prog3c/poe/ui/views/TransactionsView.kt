package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import vc.prog3c.poe.R
import vc.prog3c.poe.databinding.ActivityTransactionsBinding
import vc.prog3c.poe.ui.adapters.TransactionAdapter
import vc.prog3c.poe.ui.viewmodels.TransactionType
import vc.prog3c.poe.ui.viewmodels.TransactionViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionsView : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionsBinding
    private lateinit var viewModel: TransactionViewModel
    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[TransactionViewModel::class.java]
        setupToolbar()
        setupBottomNavigation()
        setupRecyclerView()
        setupFilterChips()
        observeViewModel()
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
                R.id.nav_transactions -> {
                    true
                }
                R.id.nav_add_income -> {
                    startActivity(Intent(this, AddIncomeView::class.java))
                    true
                }
                R.id.nav_add_expense -> {
                    startActivity(Intent(this, AddExpenseView::class.java))
                    true
                }
                else -> false
            }
        }
        binding.bottomNavigation.selectedItemId = R.id.nav_transactions
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter()
        binding.transactionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@TransactionsView)
            adapter = this@TransactionsView.adapter
        }
    }

    private fun setupFilterChips() {
        binding.filterChipGroup.setOnCheckedChangeListener { group, checkedId ->
            val chip = group.findViewById<Chip>(checkedId)
            when (chip?.id) {
                R.id.allChip -> viewModel.getTransactionsByType(TransactionType.ALL)
                R.id.incomeChip -> viewModel.getTransactionsByType(TransactionType.INCOME)
                R.id.expenseChip -> viewModel.getTransactionsByType(TransactionType.EXPENSE)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.transactions.observe(this) { transactions ->
            adapter.submitList(transactions)
        }

        viewModel.totalIncome.observe(this) { income ->
            binding.totalIncomeText.text = formatCurrency(income)
        }

        viewModel.totalExpenses.observe(this) { expenses ->
            binding.totalExpensesText.text = formatCurrency(expenses)
        }
    }

    private fun formatCurrency(amount: Double): String {
        return NumberFormat.getCurrencyInstance(Locale.getDefault()).format(amount)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 