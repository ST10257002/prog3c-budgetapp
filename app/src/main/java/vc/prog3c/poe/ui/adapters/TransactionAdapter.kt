package vc.prog3c.poe.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import vc.prog3c.poe.R
import vc.prog3c.poe.databinding.ItemTransactionBinding
import vc.prog3c.poe.ui.viewmodels.Transaction
import vc.prog3c.poe.ui.viewmodels.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())

        fun bind(transaction: Transaction) {
            binding.apply {
                transactionTitle.text = transaction.description ?: transaction.category
                transactionCategory.text = transaction.category
                transactionDate.text = dateFormat.format(transaction.date)
                transactionAmount.text = currencyFormat.format(transaction.amount)
                transactionAmount.setTextColor(
                    itemView.context.getColor(
                        when (transaction.type) {
                            TransactionType.INCOME -> R.color.green
                            TransactionType.EXPENSE -> R.color.red
                            else -> R.color.black
                        }
                    )
                )
                transactionIcon.setImageResource(
                    when (transaction.type) {
                        TransactionType.INCOME -> R.drawable.ic_income
                        TransactionType.EXPENSE -> R.drawable.ic_expense
                        else -> R.drawable.ic_expense
                    }
                )
            }
        }
    }

    private class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
} 