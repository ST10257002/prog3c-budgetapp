package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import vc.prog3c.poe.R
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import vc.prog3c.poe.databinding.ActivityTransactionDetailsBinding
import vc.prog3c.poe.ui.adapters.PhotoAdapter
import vc.prog3c.poe.ui.viewmodels.TransactionViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionDetailsBinding
    private lateinit var viewModel: TransactionViewModel
    private lateinit var photoAdapter: PhotoAdapter
    private var transactionId: String? = null
    private var accountId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transactionId = intent.getStringExtra(EXTRA_TRANSACTION_ID)
        if (transactionId == null) {
            Toast.makeText(this, "Transaction ID is required", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel = ViewModelProvider(this)[TransactionViewModel::class.java]
        accountId = intent.getStringExtra("account_id")

        setupToolbar()
        setupPhotoRecyclerView()
        observeTransaction()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Transaction Details"
    }

    private fun setupPhotoRecyclerView() {
        photoAdapter = PhotoAdapter(
            onPhotoClick = { uri ->
                // Open photo in full screen or show in image viewer
                val intent = Intent(this, PhotoViewerActivity::class.java).apply {
                    putExtra("photo_uri", uri.toString())
                }
                startActivity(intent)
            },
            onRemoveClick = { position ->
                // In transaction details, we don't allow removing photos
                // This is just a viewer
            }
        )
        binding.photoRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@TransactionDetailsActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = photoAdapter
        }
    }

    private fun observeTransaction() {
        transactionId?.let { id ->
            viewModel.getTransaction(id).observe(this) { transaction ->
                transaction?.let { updateUI(it) }
            }
        }
    }

    private fun updateUI(transaction: Transaction) {
        binding.apply {
            descriptionTextView.text = transaction.description
            amountTextView.text = formatAmount(transaction.amount)
            amountTextView.setTextColor(
                if (transaction.type == TransactionType.INCOME) 
                    getColor(R.color.income_green) 
                else 
                    getColor(R.color.expense_red)
            )
            categoryTextView.text = transaction.category
            dateTextView.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(transaction.date.toDate())

            // Update photos
            if (transaction.photoUrls.isNotEmpty()) {
                photoSection.visibility = android.view.View.VISIBLE
                photoAdapter.updatePhotos(transaction.photoUrls.map { android.net.Uri.parse(it) })
            } else {
                photoSection.visibility = android.view.View.GONE
            }
        }
    }

    private fun formatAmount(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        return format.format(amount)
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

    companion object {
        const val EXTRA_TRANSACTION_ID = "extra_transaction_id"
    }
} 