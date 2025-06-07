package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import vc.prog3c.poe.core.utils.Blogger
import vc.prog3c.poe.databinding.ActivityTransactionsBinding
import vc.prog3c.poe.ui.viewmodels.TransactionViewModel

class TransactionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionsBinding
    private lateinit var viewModel: TransactionViewModel
    private var accountId: String? = null

    private val addTransactionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Refresh transactions when a new one is added
            accountId?.let { viewModel.loadTransactions(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[TransactionViewModel::class.java]
        accountId = intent.getStringExtra("account_id")

        if (accountId == null) {
            finish()
            return
        }

        setupToolbar()
        setupTransactionsView()
        
        // Initial load of transactions
        accountId?.let { viewModel.loadTransactions(it) }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Transactions"
        }
    }

    private fun setupTransactionsView() {
        binding.transactionsView.apply {
            setViewModel(viewModel, this@TransactionsActivity, accountId ?: "")
            setOnAddTransactionClickListener {                
                val intent = Intent(this@TransactionsActivity, TransactionUpsertActivity::class.java).apply {
                    putExtra("account_id", accountId)
                }
                addTransactionLauncher.launch(intent)
            }
        }
    }

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
} 