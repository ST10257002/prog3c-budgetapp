package vc.prog3c.poe.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import vc.prog3c.poe.databinding.ItemTransactionBinding
import vc.prog3c.poe.ui.viewmodels.Transaction

class TransactionAdapter :
    ListAdapter<Transaction, TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        return TransactionViewHolder(binding)
    }


    override fun onBindViewHolder(
        holder: TransactionViewHolder, position: Int
    ) {
        holder.bind(getItem(position))
    }
} 