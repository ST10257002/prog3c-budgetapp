package vc.prog3c.poe.data.services

import vc.prog3c.poe.core.services.AchievementEngine
import vc.prog3c.poe.data.models.Account
import vc.prog3c.poe.data.models.Category
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.ui.viewmodels.AchievementViewModel

class AchievementEvaluator(
    private val userId: String,
    private val achievementViewModel: AchievementViewModel
) {
    fun run(accounts: List<Account>, transactions: List<Transaction>, categories: List<Category>) {
        val engine = AchievementEngine(
            userId = userId,
            accounts = accounts,
            transactions = transactions,
            categories = categories
        ) { id, progress ->
            achievementViewModel.updateAchievementProgress(userId, id, progress)
        }

        engine.evaluateAll()
    }

}
