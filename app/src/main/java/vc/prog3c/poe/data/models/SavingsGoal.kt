package vc.prog3c.poe.data.models

data class SavingsGoal(
    val userId: String = "",
    val minMonthlyGoal: Double = 0.0,
    val maxMonthlyGoal: Double = 0.0,
    val monthlyBudget: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
) 