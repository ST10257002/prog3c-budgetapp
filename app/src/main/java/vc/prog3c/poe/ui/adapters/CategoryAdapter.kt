package vc.prog3c.poe.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import vc.prog3c.poe.R
import vc.prog3c.poe.data.models.Category
import java.text.NumberFormat
import java.util.Locale

class CategoryAdapter(
    private val onEditClick: (Category) -> Unit,
    private val onDeleteClick: (Category) -> Unit
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    private var categoryTotals: Map<String, Double> = emptyMap()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateCategoryTotals(totals: Map<String, Double>) {
        categoryTotals = totals
        notifyDataSetChanged()
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.categoryName)
        private val typeTextView: TextView = itemView.findViewById(R.id.categoryType)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.categoryDescription)
        private val budgetLimitTextView: TextView = itemView.findViewById(R.id.categoryBudgetLimit)
        private val iconImageView: ImageButton = itemView.findViewById(R.id.categoryIcon)
        private val editButton: ImageButton = itemView.findViewById(R.id.editButton)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
        private val statusIndicator: View = itemView.findViewById(R.id.statusIndicator)

        fun bind(category: Category) {
            nameTextView.text = category.name
            typeTextView.text = category.type.name
            descriptionTextView.text = category.description
            descriptionTextView.visibility = if (category.description.isNullOrEmpty()) View.GONE else View.VISIBLE
            
            // Format budget limit with currency
            val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
            budgetLimitTextView.text = formatter.format(category.budgetLimit)
            budgetLimitTextView.visibility = if (category.budgetLimit > 0) View.VISIBLE else View.GONE

            // Set icon and color
            val iconResId = when (category.icon) {
                "ic_category" -> R.drawable.ic_category
                "ic_savings" -> R.drawable.ic_savings
                "ic_utilities" -> R.drawable.ic_utilities
                "ic_error" -> R.drawable.ic_error
                "ic_income" -> R.drawable.ic_income
                "ic_expense" -> R.drawable.ic_expense
                else -> R.drawable.ic_category
            }
            iconImageView.setImageResource(iconResId)

            val colorResId = when (category.color) {
                "colorGreen" -> R.color.colorGreen
                "colorBlue" -> R.color.colorBlue
                "colorRed" -> R.color.colorRed
                "colorPurple" -> R.color.colorPurple
                "colorOrange" -> R.color.colorOrange
                else -> R.color.colorGreen
            }
            iconImageView.setColorFilter(ContextCompat.getColor(itemView.context, colorResId))

            // Set status indicator
            statusIndicator.setBackgroundColor(
                ContextCompat.getColor(
                    itemView.context,
                    if (category.isActive) R.color.status_success else R.color.status_error
                )
            )

            // Set edit and delete button visibility
            editButton.visibility = if (category.isEditable) View.VISIBLE else View.GONE
            deleteButton.visibility = if (category.isEditable) View.VISIBLE else View.GONE

            editButton.setOnClickListener { onEditClick(category) }
            deleteButton.setOnClickListener { onDeleteClick(category) }
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
