package vc.prog3c.poe.core.services

import vc.prog3c.poe.data.models.Account
import vc.prog3c.poe.data.models.Category
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType

class AchievementEngine(
    private val userId: String,
    private val accounts: List<Account>,
    private val transactions: List<Transaction>,
    private val categories: List<Category>,
    private val updateProgress: (achievementId: String, progress: Int) -> Unit
) {

    fun evaluateAll() {
        evaluateBigSaver()
        evaluateFirstExpense()
        evaluateEmergencyFundBuilder()
        // Add more here...
    }

    private fun evaluateBigSaver() {
        val totalSaved = accounts
            .filter { it.type.equals("Savings", ignoreCase = true) }
            .sumOf { it.balance }

        updateProgress("big_saver", totalSaved.toInt())
    }

    private fun evaluateFirstExpense() {
        val hasExpense = transactions.any { it.type == TransactionType.EXPENSE }
        updateProgress("first_expense", if (hasExpense) 1 else 0)
    }

    private fun evaluateEmergencyFundBuilder() {
        val emergencyTotal = transactions
            .filter { it.category.equals("Emergency", ignoreCase = true) }
            .sumOf { it.amount }

        val emergencyGoal = categories
            .find { it.name.equals("Emergency", ignoreCase = true) }
            ?.maxBudget ?: 1000.0

        updateProgress("emergency_fund", emergencyTotal.coerceAtMost(emergencyGoal).toInt())
    }



    // TODO: Add more evaluation methods like:
    // evaluateDailyTracker(), evaluateWeeklyWarrior(), etc.
}
