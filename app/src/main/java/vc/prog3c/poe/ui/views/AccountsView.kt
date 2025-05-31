package vc.prog3c.poe.ui.views

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import vc.prog3c.poe.R
import vc.prog3c.poe.core.utils.Blogger
import vc.prog3c.poe.data.models.Account
import vc.prog3c.poe.databinding.ActivityAccountsBinding
import vc.prog3c.poe.ui.adapters.AccountAdapter
import vc.prog3c.poe.ui.viewmodels.AccountsUiState
import vc.prog3c.poe.ui.viewmodels.AccountsViewModel
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

class AccountsView : AppCompatActivity(), View.OnClickListener {

    private lateinit var binds: ActivityAccountsBinding
    private lateinit var model: AccountsViewModel
    private lateinit var accountAdapter: AccountAdapter


    // --- Lifecycle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBindings()
        setupLayoutUi()
        setupClickListeners()

        model = ViewModelProvider(this)[AccountsViewModel::class.java]

        observeViewModel()

        model.fetchAccounts()
    }


    // --- ViewModel


    private fun observeViewModel() = model.uiState.observe(this) { state ->
        when (state) {
            is AccountsUiState.Default -> {
                binds.swipeRefreshLayout.isRefreshing = false
            }

            is AccountsUiState.Loading -> {
                binds.swipeRefreshLayout.isRefreshing = true
            }

            is AccountsUiState.Updated -> {
                binds.swipeRefreshLayout.isRefreshing = false

                state.accounts?.let {
                    accountAdapter.submitList(it)
                    updateNetWorthPieChart(it)
                }

                state.netWorth?.let {
                    binds.netWorthAmount.text = NumberFormat.getCurrencyInstance(
                        Locale.getDefault()
                    ).format(it)
                }
            }

            is AccountsUiState.Failure -> {
                binds.swipeRefreshLayout.isRefreshing = false
                Snackbar.make(
                    binds.root, state.message, Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }
    
    
    // --- Internals


    private fun showAddAccountDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_account, null)
        val nameField = dialogView.findViewById<TextInputEditText>(R.id.accountNameEditText)
        val typeField =
            dialogView.findViewById<AutoCompleteTextView>(R.id.accountTypeAutoCompleteTextView)
        val saveBtn = dialogView.findViewById<Button>(R.id.saveAccountButton)

        val types = listOf("Credit", "Debit", "Savings")
        typeField.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, types))

        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        saveBtn.setOnClickListener {
            val name = nameField.text.toString().trim()
            val type = typeField.text.toString().trim()
            if (name.isEmpty()) {
                nameField.error = "Required"; return@setOnClickListener
            }
            if (type.isEmpty() || type !in types) {
                typeField.error = "Invalid"; return@setOnClickListener
            }

            // ⚡️ REAL USER ID
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener run {
                Snackbar.make(binds.root, "Not signed in", Snackbar.LENGTH_SHORT).show()
            }

            val newAccount = Account(
                id = UUID.randomUUID().toString(),
                userId = uid,
                name = name,
                type = type,
                balance = 0.0,
                transactionsCount = 0
            )
            model.addAccount(newAccount)
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun setupNetWorthPieChart() {
        binds.netWorthPieChart.apply {
            description.isEnabled = false
            isRotationEnabled = false
            centerText = "Net Worth"
            setHoleColor(Color.TRANSPARENT)
            setTransparentCircleAlpha(0)
            legend.apply {
                isEnabled = true
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                xEntrySpace = 7f
                yEntrySpace = 0f
                yOffset = 0f
            }
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            animateY(1000)
        }
    }


    private fun updateNetWorthPieChart(
        accounts: List<Account>
    ) {
        val entries = accounts.map {
            PieEntry(it.balance.toFloat(), it.name)
        }.filter { it.value > 0f }

        if (entries.isEmpty()) {
            binds.netWorthPieChart.data = null
            binds.netWorthPieChart.invalidate()
            return
        }

        val ds = PieDataSet(entries, "Account Distribution").apply {
            valueTextColor = Color.BLACK
            valueTextSize = 12f
            valueFormatter = PercentFormatter(binds.netWorthPieChart)
            colors = listOf(
                resources.getColor(R.color.primary, null),
                resources.getColor(R.color.green, null),
                resources.getColor(R.color.red, null),
                "#FF9800".toColorInt(),
                "#9C27B0".toColorInt()
            )
        }

        binds.netWorthPieChart.data = PieData(ds)
        binds.netWorthPieChart.invalidate()
        binds.netWorthPieChart.animateY(1000)
    }


    // --- Event Handlers


    override fun onClick(view: View?) {
        when (view?.id) {
            binds.addAccountButton.id -> showAddAccountDialog()
        }
    }


    private fun setupClickListeners() {
        binds.addAccountButton.setOnClickListener(this)
    }


    // --- UI Configuration


    private fun setupToolbar() {
        setSupportActionBar(binds.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Accounts"
    }


    private fun setupBottomNavigation() {
        binds.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, DashboardView::class.java))
                    finish()
                    true
                }

                R.id.nav_accounts -> true // already here
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


    private fun setupAccountsRecyclerView() {
        accountAdapter = AccountAdapter()
        binds.accountsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AccountsView)
            adapter = accountAdapter
        }
        accountAdapter.setOnItemClickListener { account ->
            val intent = Intent(this, AccountDetailsView::class.java)
            intent.putExtra("account_id", account.id)
            startActivity(intent)
        }
    }


    private fun setupSwipeRefresh() {
        binds.swipeRefreshLayout.apply {
            setColorSchemeResources(R.color.primary, R.color.green, R.color.red)
            setOnRefreshListener {
                model.fetchAccounts()
            }
        }
    }


    // --- UI Registrations


    private fun setupBindings() {
        binds = ActivityAccountsBinding.inflate(layoutInflater)
    }


    private fun setupLayoutUi() {
        setContentView(binds.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binds.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupBottomNavigation()
        setupSwipeRefresh()
        setupAccountsRecyclerView()
        setupNetWorthPieChart()
    }
}
