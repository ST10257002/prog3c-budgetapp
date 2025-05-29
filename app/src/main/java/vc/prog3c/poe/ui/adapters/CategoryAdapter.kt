package vc.prog3c.poe.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import vc.prog3c.poe.R
import vc.prog3c.poe.data.models.Category
import vc.prog3c.poe.data.models.CategoryType
import vc.prog3c.poe.databinding.ItemCategoryBinding
import java.text.NumberFormat
import java.util.Locale

class CategoryAdapter(
    private val onEditClick: (Category) -> Unit,
    private val onDeleteClick: (Category) -> Unit
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
    private var categoryTotals: Map<String, Double> = emptyMap()

    fun updateCategoryTotals(totals: Map<String, Double>) {
        categoryTotals = totals
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryViewHolder(
        private val binding: ItemCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.apply {
                categoryName.text = category.name
                categoryType.text = category.type.toString()
                
                // Set category icon based on type
                categoryIcon.setImageResource(
                    when (category.type) {
                        CategoryType.SAVINGS -> R.drawable.ic_savings
                        CategoryType.EMERGENCY -> R.drawable.ic_error
                        CategoryType.UTILITIES -> R.drawable.ic_utilities
                        else -> R.drawable.ic_category
                    }
                )

                // Set total amount for this category
                val total = categoryTotals[category.name] ?: 0.0
                categoryTotal.text = currencyFormat.format(total)
                
                // Only show edit/delete buttons for custom categories
                if (category.type == CategoryType.CUSTOM) {
                    editButton.visibility = View.VISIBLE
                    deleteButton.visibility = View.VISIBLE
                    editButton.setOnClickListener { onEditClick(category) }
                    deleteButton.setOnClickListener { onDeleteClick(category) }
                } else {
                    editButton.visibility = View.GONE
                    deleteButton.visibility = View.GONE
                }
            }
        }
    }

    private class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }
    }
}
