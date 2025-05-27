package vc.prog3c.poe.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import vc.prog3c.poe.R
import vc.prog3c.poe.data.models.Category
import java.text.NumberFormat
import java.util.Locale

class CategoryAdapter(
    private var categories: List<Category>,
    private var categoryAmounts: Map<String, Double> = emptyMap()
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryName: TextView = view.findViewById(R.id.categoryName)
        val categoryAmount: TextView = view.findViewById(R.id.categoryAmount)
        val categoryIcon: ImageView = view.findViewById(R.id.categoryIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        val amount = categoryAmounts[category.name] ?: 0.0
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())

        holder.categoryName.text = category.name
        holder.categoryAmount.text = currencyFormat.format(amount)

        // Set the icon and color for the ImageView
        val iconResId = holder.itemView.context.resources.getIdentifier(
            category.icon,
            "drawable",
            holder.itemView.context.packageName
        )
        if (iconResId != 0) {
            holder.categoryIcon.setImageResource(iconResId)
            try {
                val color = android.graphics.Color.parseColor(category.color)
                holder.categoryIcon.setColorFilter(color)
            } catch (e: IllegalArgumentException) {
                // Handle invalid color format if necessary
                holder.categoryIcon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.black))
            }
        } else {
            // Handle case where icon resource is not found, maybe set a default icon
            holder.categoryIcon.setImageResource(R.drawable.ic_category_default) // Assuming a default icon exists
            holder.categoryIcon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.black))
        }
    }

    override fun getItemCount() = categories.size

    fun updateCategories(newCategories: List<Category>, newAmounts: Map<String, Double>) {
        categories = newCategories
        categoryAmounts = newAmounts
        notifyDataSetChanged()
    }
} 