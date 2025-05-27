package vc.prog3c.poe.ui.views

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.Legend // Correct import
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import vc.prog3c.poe.R
import vc.prog3c.poe.data.models.Account // Correct import
import vc.prog3c.poe.databinding.ActivityAccountsBinding
import vc.prog3c.poe.ui.adapters.AccountAdapter
import vc.prog3c.poe.ui.viewmodels.AccountsViewModel // Correct import
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

class AccountsView : AppCompatActivity() {
    private lateinit var binding: ActivityAccountsBinding
    private lateinit var viewModel: AccountsViewModel
    private lateinit var accountAdapter: AccountAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AccountsViewModel::class.java]

        setupToolbar()
        setupBottomNavigation()
        setupSwipeRefresh()
        setupAccountsRecyclerView()
        setupAddAccountButton()
        setupNetWorthPieChart()
        observeViewModel()

        viewModel.loadAccounts()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Accounts"
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
                    true
                }
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
        binding.bottomNavigation.selectedItemId = R.id.nav_accounts
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
        viewModel.loadAccounts()
    }

    private fun setupAccountsRecyclerView() {
        accountAdapter = AccountAdapter()
        binding.accountsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AccountsView)
            adapter = accountAdapter
        }
        accountAdapter.setOnItemClickListener { account ->
            val intent = Intent(this, AccountDetailsView::class.java)
            intent.putExtra("account_id", account.id)
            startActivity(intent)
        }
    }

    private fun setupAddAccountButton() {
        binding.addAccountButton.setOnClickListener {
            showAddAccountDialog()
        }
    }

    private fun showAddAccountDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_account, null)
        val accountNameEditText = dialogView.findViewById<TextInputEditText>(R.id.accountNameEditText)
        val accountTypeAutoCompleteTextView = dialogView.findViewById<AutoCompleteTextView>(R.id.accountTypeAutoCompleteTextView)
        val saveButton = dialogView.findViewById<Button>(R.id.saveAccountButton)

        val accountTypes = listOf("Credit", "Debit", "Savings")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, accountTypes)
        accountTypeAutoCompleteTextView.setAdapter(adapter)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            val accountName = accountNameEditText.text.toString().trim()
            val accountType = accountTypeAutoCompleteTextView.text.toString().trim()

            if (accountName.isEmpty()) {
                accountNameEditText.error = "Account name cannot be empty"
                return@setOnClickListener
            }

            if (accountType.isEmpty()) {
                accountTypeAutoCompleteTextView.error = "Account type cannot be empty"
                return@setOnClickListener
            }

            // Optional: Validate if the entered account type is one of the predefined types
            if (!accountTypes.contains(accountType)) {
                accountTypeAutoCompleteTextView.error = "Invalid account type"
                return@setOnClickListener
            }

            // TODO: Get actual user ID
            val newAccount = Account(
                id = UUID.randomUUID().toString(), // Temporary ID
                userId = "user1", // Replace with actual user ID
                name = accountName,
                type = accountType,
                balance = 0.0, // New accounts start with 0 balance
                transactionsCount = 0 // Initialize with 0 transactions
            )
            viewModel.addAccount(newAccount)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupNetWorthPieChart() {
        binding.netWorthPieChart.apply {
            description.isEnabled = false
            isRotationEnabled = false
            centerText = "Net Worth"
            setHoleColor(Color.TRANSPARENT)
            setTransparentCircleAlpha(0)
            legend.isEnabled = true // Enable legend to show account names
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM)
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT)
            legend.setOrientation(Legend.LegendOrientation.HORIZONTAL)
            legend.setDrawInside(false)
            legend.setXEntrySpace(7f)
            legend.setYEntrySpace(0f)
            legend.setYOffset(0f)

            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)

            animateY(1000)
        }
    }

    private fun observeViewModel() {
        viewModel.accounts.observe(this) { accounts ->
            accountAdapter.submitList(accounts)
            updateNetWorthPieChart(accounts)
        }

        viewModel.netWorth.observe(this) { netWorth ->
            binding.netWorthAmount.text = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(netWorth)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
            // TODO: Show/hide loading indicator for the list if not using SwipeRefreshLayout
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                showError(it)
            }
        }
    }

    private fun updateNetWorthPieChart(accounts: List<Account>) {
        val entries = accounts.map { account ->
            // Use absolute value for pie chart as it represents a portion of total assets
            // Filter out accounts with zero balance if they shouldn't be in the chart
            PieEntry(account.balance.toFloat(), account.name)
        }.filter { it.value.isFinite() && it.value > 0f } // Filter out non-finite and zero values

        if (entries.isEmpty()) {
            binding.netWorthPieChart.data = null
            binding.netWorthPieChart.invalidate()
            return
        }

        val dataSet = PieDataSet(entries, "Account Distribution")
        dataSet.colors = mutableListOf(
            resources.getColor(R.color.primary, null),
            resources.getColor(R.color.green, null),
            resources.getColor(R.color.red, null),
            Color.parseColor("#FF9800"), // Orange
            Color.parseColor("#9C27B0")  // Purple
            // Add more colors if you expect more accounts
        )
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f
        dataSet.valueFormatter = PercentFormatter(binding.netWorthPieChart)
        dataSet.setDrawValues(true) // Draw percentage values on slices

        val pieData = PieData(dataSet)
        binding.netWorthPieChart.data = pieData
        binding.netWorthPieChart.invalidate()
        binding.netWorthPieChart.animateY(1000)
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Retry") {
                refreshData()
            }
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}