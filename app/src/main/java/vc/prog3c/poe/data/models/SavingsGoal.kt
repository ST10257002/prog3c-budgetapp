package vc.prog3c.poe.data.models

import java.util.Date

data class SavingsGoal(
    val id: String = "", // Document ID of the savings goal
    val userId: String = "", // UID of the user (optional, since it’s in the path)
    val name: String = "",
    val targetAmount: Double = 0.0,
    val savedAmount: Double = 0.0,
    val targetDate: Date? = null,
    val minMonthlyGoal: Double = 0.0,
    val maxMonthlyGoal: Double = 0.0,
    val monthlyBudget: Double = 0.0
)