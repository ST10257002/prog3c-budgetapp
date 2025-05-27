package vc.prog3c.poe.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import vc.prog3c.poe.R
import vc.prog3c.poe.data.models.Account // Correct import
import java.text.NumberFormat
import java.util.Locale

class AccountAdapter : ListAdapter<Account, AccountAdapter.AccountViewHolder>(AccountDiffCallback()) {

    private var onItemClickListener: ((Account) -> Unit)? = null

    fun setOnItemClickListener(listener: (Account) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_account, parent, false)
        return AccountViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val account = getItem(position)
        holder.bind(account)

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(account)
        }
    }

    class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val accountNameTextView: TextView = itemView.findViewById(R.id.accountNameTextView)
        private val accountTransactionsTextView: TextView = itemView.findViewById(R.id.accountTransactionsTextView)
        private val accountBalanceTextView: TextView = itemView.findViewById(R.id.accountBalanceTextView)
        private val accountIconImageView: ImageView = itemView.findViewById(R.id.accountIcon)

        fun bind(account: Account) {
            accountNameTextView.text = account.name
            accountTransactionsTextView.text = "${account.transactionsCount} transactions"
            accountBalanceTextView.text = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(account.balance)

            val iconResId = when (account.type.lowercase()) { // Use lowercase()
                "credit" -> R.drawable.ic_credit_card
                "savings" -> R.drawable.ic_savings
                else -> R.drawable.ic_account_balance
            }
            accountIconImageView.setImageResource(iconResId)
        }
    }

    private class AccountDiffCallback : DiffUtil.ItemCallback<Account>() {
        override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem == newItem
        }
    }
}