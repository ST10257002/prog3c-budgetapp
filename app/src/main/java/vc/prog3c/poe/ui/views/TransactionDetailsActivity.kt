package vc.prog3c.poe.ui.views

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import vc.prog3c.poe.R
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.databinding.ActivityTransactionDetailsBinding
import vc.prog3c.poe.ui.viewmodels.TransactionViewModel
import java.text.NumberFormat
import java.util.*

class TransactionDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionDetailsBinding
    private lateinit var viewModel: TransactionViewModel
    private var transactionId: String? = null
    private var accountId: String? = null

    companion object {
        const val EXTRA_TRANSACTION_ID = "extra_transaction_id"
        const val EXTRA_ACCOUNT_ID = "extra_account_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get transaction ID and account ID from intent
        transactionId = intent.getStringExtra(EXTRA_TRANSACTION_ID)
        accountId = intent.getStringExtra(EXTRA_ACCOUNT_ID)

        if (transactionId == null) {
            Toast.makeText(this, "Transaction ID is required", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupViewModel()
        observeTransaction()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Transaction Details"
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[TransactionViewModel::class.java]
    }

    private fun observeTransaction() {
        transactionId?.let { id ->
            viewModel.getTransaction(id).observe(this) { transaction ->
                if (transaction == null) {
                    Toast.makeText(this, "Transaction not found", Toast.LENGTH_SHORT).show()
                    finish()
                    return@observe
                }
                displayTransactionDetails(transaction)
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displayTransactionDetails(transaction: Transaction) {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
        
        binding.apply {
            // Set transaction type and amount
            val amountText = currencyFormat.format(transaction.amount)
            amountTextView.text = amountText
            typeTextView.text = transaction.type.name

            // Set description
            descriptionTextView.text = transaction.description ?: "No description"

            // Set date
            val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            dateTextView.text = dateFormat.format(transaction.date.toDate())

            // Set category
            categoryTextView.text = transaction.category

            // Load photo if exists
            if (transaction.photoUrls.isNotEmpty()) {
                val requestOptions = RequestOptions()
                    .placeholder(R.drawable.ic_photo_placeholder)
                    .error(R.drawable.ic_photo_placeholder)

                Glide.with(this@TransactionDetailsActivity)
                    .load(android.net.Uri.parse(transaction.photoUrls.first()))
                    .apply(requestOptions)
                    .into(photoImageView)
            } else {
                photoImageView.setImageResource(R.drawable.ic_photo_placeholder)
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
} 