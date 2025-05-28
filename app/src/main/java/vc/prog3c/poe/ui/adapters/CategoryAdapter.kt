package vc.prog3c.poe.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vc.prog3c.poe.R
import java.text.NumberFormat
import java.util.*

class CategoryAdapter(
    private var categoryAmounts: Map<String, Double>
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryName: TextView = view.findViewById(R.id.categoryName)
        val categoryAmount: TextView = view.findViewById(R.id.categoryAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoryAmounts.keys.elementAt(position)
        val amount = categoryAmounts[category] ?: 0.0
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())

        holder.categoryName.text = category
        holder.categoryAmount.text = currencyFormat.format(amount)
    }

    override fun getItemCount(): Int = categoryAmounts.size

    fun updateCategoryData(newData: Map<String, Double>) {
        categoryAmounts = newData
        notifyDataSetChanged()
    }
}
