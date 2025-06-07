package vc.prog3c.poe.ui.views

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import vc.prog3c.poe.R
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.databinding.ActivityTransactionDetailsBinding
import vc.prog3c.poe.ui.viewmodels.TransactionViewModel
import java.text.NumberFormat
import java.util.*
import androidx.core.net.toUri

class TransactionDetailsActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_TRANSACTION_ID = "extra_transaction_id"
        const val EXTRA_ACCOUNT_ID = "extra_account_id"
    }
    
    private lateinit var binds: ActivityTransactionDetailsBinding
    private lateinit var model: TransactionViewModel
    
    private var transactionId: String? = null
    private var accountId: String? = null
    
    // --- Lifecycle
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setupBindings()
        setupLayoutUi()

        model = ViewModelProvider(this)[TransactionViewModel::class.java]

        // Get transaction ID and account ID from intent
        transactionId = intent.getStringExtra(EXTRA_TRANSACTION_ID)
        accountId = intent.getStringExtra(EXTRA_ACCOUNT_ID)

        if (transactionId == null) {
            Toast.makeText(
                this, "Transaction ID is required", Toast.LENGTH_SHORT
            ).show()
            finish()
        }
        
        observeTransaction()
    }

    // --- ViewModel

    private fun observeTransaction() {
        transactionId?.let { id ->
            model.getTransaction(id).observe(this) { transaction ->
                if (transaction == null) {
                    Toast.makeText(
                        this, "Transaction not found", Toast.LENGTH_SHORT
                    ).show()
                    finish()
                    
                    return@observe
                }
                displayTransactionDetails(transaction)
            }
        }

        model.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    // --- Internals

    private fun displayTransactionDetails(transaction: Transaction) {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
        
        binds.apply {
            // Set transaction type and amount
            val amountText = currencyFormat.format(transaction.amount)
            tvCost.text = amountText
            tvType.text = transaction.type.name

            // Set description
            descriptionTextView.text = transaction.description

            // Set date
            val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            dateTextView.text = dateFormat.format(transaction.date.toDate())

            // Set category
            categoryTextView.text = transaction.category

            // Load photo if exists
            if (transaction.imageUri.isNotEmpty()) {
                val requestOptions = RequestOptions()
                    .placeholder(R.drawable.ic_photo_placeholder)
                    .error(R.drawable.ic_photo_placeholder)

                Glide.with(ivImage.context).apply { 
                    load(transaction.imageUri.toUri()).apply(requestOptions).into(ivImage)
                }
            } else {
                ivImage.setImageResource(R.drawable.ic_photo_placeholder)
            }
        }
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
            title = "Transaction Details"
        }
    }

    // --- UI Registrations

    private fun setupBindings() {
        binds = ActivityTransactionDetailsBinding.inflate(layoutInflater)
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