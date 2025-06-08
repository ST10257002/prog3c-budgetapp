package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import vc.prog3c.poe.databinding.ActivityTransactionsBinding
import vc.prog3c.poe.ui.viewmodels.TransactionViewModel

class TransactionsActivity : AppCompatActivity() {
    
    private lateinit var binds: ActivityTransactionsBinding
    private lateinit var model: TransactionViewModel
    private var accountId: String? = null

    private val addTransactionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Refresh transactions when a new one is added
            accountId?.let { model.loadTransactions(it) }
        }
    }
    
    // --- Lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setupBindings()
        setupLayoutUi()

        model = ViewModelProvider(this)[TransactionViewModel::class.java]
        
        setupTransactionsView()
        
        accountId = intent.getStringExtra("account_id")
        if (accountId == null) {
            Toast.makeText(
                this, "Account ID is required", Toast.LENGTH_SHORT
            ).show()
            finish()
        } else {
            model.loadAccountName(accountId!!)
            model.loadTransactions(accountId!!)
        }
        
        observeViewModel()
    }
    
    // --- ViewModel
    
    private fun observeViewModel() {
        model.accountName.observe(this) { name ->
            supportActionBar?.subtitle = name ?: ""
        }
    }

    // --- Internals

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    // --- UI Configuration

    private fun setupToolbar() {
        setSupportActionBar(binds.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Transactions"
        }
    }

    private fun setupTransactionsView() {
        binds.transactionsView.apply {
            setViewModel(model, this@TransactionsActivity, accountId ?: "")
            setOnAddTransactionClickListener {
                val intent = Intent(this@TransactionsActivity, TransactionUpsertActivity::class.java).apply {
                    putExtra("account_id", accountId)
                }
                addTransactionLauncher.launch(intent)
            }
        }
    }
    
    // --- UI Registrations
    
    private fun setupBindings() {
        binds = ActivityTransactionsBinding.inflate(layoutInflater)
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
    }
} 