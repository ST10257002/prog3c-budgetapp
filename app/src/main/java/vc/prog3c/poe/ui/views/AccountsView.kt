package vc.prog3c.poe.ui.views

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import vc.prog3c.poe.R
import vc.prog3c.poe.data.models.Account
import vc.prog3c.poe.databinding.ActivityAccountsBinding
import vc.prog3c.poe.ui.adapters.AccountAdapter
import vc.prog3c.poe.ui.viewmodels.AccountsViewModel
import java.text.NumberFormat
import java.util.*

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
        setupBottomNav()
        setupRecyclerView()
        setupSwipeRefresh()
        setupAddAccountDialog()
        setupPieChart()
        observeViewModel()

        viewModel.fetchAccounts()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Accounts"
    }

    private fun setupBottomNav() {
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, DashboardView::class.java))
                    finish(); true
                }
                R.id.nav_graph -> {
                    startActivity(Intent(this, GraphView::class.java))
                    finish(); true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish(); true
                }
                else -> false
            }
        }
        binding.bottomNavigation.selectedItemId = R.id.nav_accounts
    }

    private fun setupRecyclerView() {
        accountAdapter = AccountAdapter()
        binding.accountsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.accountsRecyclerView.adapter = accountAdapter

        accountAdapter.setOnItemClickListener { account ->
            val intent = Intent(this, AccountDetailsView::class.java)
            intent.putExtra("account_id", account.id)
            startActivity(intent)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchAccounts()
        }
    }

    private fun setupAddAccountDialog() {
        binding.addAccountButton.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_account, null)
            val nameInput = dialogView.findViewById<TextInputEditText>(R.id.accountNameEditText)
            val typeInput = dialogView.findViewById<AutoCompleteTextView>(R.id.accountTypeAutoCompleteTextView)
            val saveButton = dialogView.findViewById<Button>(R.id.saveAccountButton)

            val types = listOf("Credit", "Debit", "Savings")
            typeInput.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, types))

            val dialog = AlertDialog.Builder(this).setView(dialogView).create()

            saveButton.setOnClickListener {
                val name = nameInput.text.toString().trim()
                val type = typeInput.text.toString().trim()

                if (name.isEmpty()) {
                    nameInput.error = "Required"
                    return@setOnClickListener
                }

                if (type.isEmpty() || type !in types) {
                    typeInput.error = "Invalid"
                    return@setOnClickListener
                }

                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid == null) {
                    Snackbar.make(binding.root, "Not signed in", Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val account = Account(
                    id = UUID.randomUUID().toString(),
                    userId = uid,
                    name = name,
                    type = type,
                    balance = 0.0,
                    transactionsCount = 0
                )

                viewModel.addAccount(account)
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun setupPieChart() {
        binding.netWorthPieChart.apply {
            description.isEnabled = false
            isRotationEnabled = false
            centerText = "Net Worth"
            setHoleColor(Color.TRANSPARENT)
            setTransparentCircleAlpha(0)
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            animateY(1000)
        }
    }

    private fun observeViewModel() {
        viewModel.accounts.observe(this) { accounts ->
            accountAdapter.submitList(accounts)
            updatePieChart(accounts)
        }

        viewModel.netWorth.observe(this) { total ->
            binding.netWorthAmount.text =
                NumberFormat.getCurrencyInstance(Locale.getDefault()).format(total)
        }

        viewModel.isLoading.observe(this) {
            binding.swipeRefreshLayout.isRefreshing = it
        }

        viewModel.error.observe(this) {
            it?.let { msg ->
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun updatePieChart(accounts: List<Account>) {
        val entries = accounts.filter { it.balance > 0 }.map {
            PieEntry(it.balance.toFloat(), it.name)
        }

        if (entries.isEmpty()) {
            binding.netWorthPieChart.clear()
            return
        }

        val ds = PieDataSet(entries, "Distribution").apply {
            valueTextSize = 12f
            valueTextColor = Color.BLACK
            valueFormatter = PercentFormatter(binding.netWorthPieChart)
            colors = listOf(
                getColor(R.color.primary),
                getColor(R.color.green),
                getColor(R.color.red),
                Color.parseColor("#FF9800"),
                Color.parseColor("#9C27B0")
            )
        }

        binding.netWorthPieChart.data = PieData(ds)
        binding.netWorthPieChart.invalidate()
    }
}
