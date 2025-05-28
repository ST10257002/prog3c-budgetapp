package vc.prog3c.poe.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import vc.prog3c.poe.R
import vc.prog3c.poe.data.models.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter :
    ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = TransactionViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
    )

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.transactionTitle)
        private val category: TextView = view.findViewById(R.id.transactionCategory)
        private val date: TextView = view.findViewById(R.id.transactionDate)
        private val amount: TextView = view.findViewById(R.id.transactionAmount)

        fun bind(item: Transaction) {
            title.text = item.description ?: "No description"
            category.text = item.category
            date.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(item.date.toDate())
            amount.text = NumberFormat.getCurrencyInstance().format(item.amount)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction) = oldItem == newItem
    }
}
