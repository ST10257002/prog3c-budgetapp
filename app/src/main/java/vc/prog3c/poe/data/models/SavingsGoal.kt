package vc.prog3c.poe.data.models

import java.util.Date

data class SavingsGoal(
    val id: String = "", // Document ID of the savings goal
    val userId: String = "", // UID of the user (optional, since it's in the path)
    val name: String = "",
    val targetAmount: Double = 0.0,
    val savedAmount: Double = 0.0,
    val targetDate: Date? = null,
    val minMonthlyGoal: Double = 0.0,
    val maxMonthlyGoal: Double = 0.0,
    val monthlyBudget: Double = 0.0,
    val monthlyContribution: Double = 0.0,
    val lastContributionDate: Date? = null
) {
    fun calculateProgress(): Double {
        return if (targetAmount > 0) (savedAmount / targetAmount) * 100 else 0.0
    }

    fun calculateRemainingAmount(): Double {
        return targetAmount - savedAmount
    }

    fun calculateMonthlyContribution(): Double {
        return if (targetDate != null) {
            val monthsRemaining = calculateMonthsRemaining()
            if (monthsRemaining > 0) calculateRemainingAmount() / monthsRemaining else 0.0
        } else {
            monthlyContribution
        }
    }

    private fun calculateMonthsRemaining(): Int {
        if (targetDate == null) return 0
        val currentDate = Date()
        val diffInMillis = targetDate.time - currentDate.time
        return (diffInMillis / (1000L * 60 * 60 * 24 * 30)).toInt()
    }
}