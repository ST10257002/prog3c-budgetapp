package vc.prog3c.poe.data.models

data class MonthlyStats(
    val totalExpenses: Double = 0.0,
    val budget: Double = 0.0
) {
    fun calculateSpendingPercentage(): Double {
        return if (budget > 0) (totalExpenses / budget) * 100 else 0.0
    }

    fun calculateRemainingBudget(): Double {
        return budget - totalExpenses
    }
} 