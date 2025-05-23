package vc.prog3c.poe.ui.adapters

import androidx.recyclerview.widget.DiffUtil
import vc.prog3c.poe.ui.viewmodels.Transaction

class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {

    override fun areItemsTheSame(
        oldItem: Transaction, newItem: Transaction
    ): Boolean {
        return oldItem.id == newItem.id
    }


    override fun areContentsTheSame(
        oldItem: Transaction, newItem: Transaction
    ): Boolean {
        return oldItem == newItem
    }
}