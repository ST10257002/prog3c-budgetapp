package vc.prog3c.poe.ui.views
/**
 * @reference Android ActivityResultLauncher: https://developer.android.com/training/basics/intents/result
 * @reference Jetpack ViewModel with LiveData Observers: https://developer.android.com/topic/libraries/architecture/livedata
 * @reference Toolbar and Navigation Patterns: https://developer.android.com/training/appbar
 */

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
import vc.prog3c.poe.ui.viewmodels.AchievementViewModel
import vc.prog3c.poe.ui.viewmodels.TransactionViewModel

class TransactionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionsBinding
    private lateinit var viewModel: TransactionViewModel
    private lateinit var achievementViewModel: AchievementViewModel

    private var accountId: String? = null

    private val addTransactionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            accountId?.let { viewModel.loadTransactions(it) }
        }
    }

    // --- Lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModels
        viewModel = ViewModelProvider(this)[TransactionViewModel::class.java]
        achievementViewModel = ViewModelProvider(this)[AchievementViewModel::class.java]

        accountId = intent.getStringExtra("account_id")
        if (accountId == null) {
            finish()
            return
        }

        setupToolbar()
        setupTransactionsView()

        // Initial load of transactions
        accountId?.let { viewModel.loadTransactions(it) }

        observeViewModel()
    }

    // --- ViewModel

    private fun observeViewModel() {
        viewModel.accountName.observe(this) { name ->
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
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Transactions"
        }
    }

    private fun setupTransactionsView() {
        binding.transactionsView.apply {
            setViewModel(viewModel, achievementViewModel, this@TransactionsActivity, accountId ?: "")
            setOnAddTransactionClickListener {
                val intent = Intent(this@TransactionsActivity, TransactionUpsertActivity::class.java).apply {
                    putExtra("account_id", accountId)
                }
                addTransactionLauncher.launch(intent)
            }
        }
    }

    // --- UI Registrations

    private fun setupLayoutUi() {
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
    }
}
