package vc.prog3c.poe.data.models

data class GraphBarEntry(
    val categoryId: String,
    val categoryName: String,
    val totalSpent: Double,
    val minBudget: Double,
    val maxBudget: Double
)
