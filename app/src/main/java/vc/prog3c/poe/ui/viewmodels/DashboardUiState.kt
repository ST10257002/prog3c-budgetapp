package vc.prog3c.poe.ui.viewmodels

import vc.prog3c.poe.data.models.Budget
import vc.prog3c.poe.data.models.Category
import vc.prog3c.poe.data.models.MonthlyStats
import vc.prog3c.poe.data.models.SavingsGoal

sealed interface DashboardUiState {
    object Default : DashboardUiState
    object Loading : DashboardUiState

    data class Updated(
        val categoryList: List<Category>? = null,
        val breakdowns: Map<String, Double>? = null,
        val statistics: MonthlyStats? = null,
        val savingsGoals: List<SavingsGoal>? = null,
        val budget: Budget? = null
    ) : DashboardUiState

    data class Failure(val message: String) : DashboardUiState
}