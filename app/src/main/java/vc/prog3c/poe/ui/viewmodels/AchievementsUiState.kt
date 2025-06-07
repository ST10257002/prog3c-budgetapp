package vc.prog3c.poe.ui.viewmodels

import vc.prog3c.poe.data.models.Achievement
import vc.prog3c.poe.data.models.BoosterBucks

sealed interface AchievementsUiState {
    object Default : AchievementsUiState
    object Loading : AchievementsUiState
    
    data class Updated(
        val achievements: List<Achievement>? = null,
        val boosterBucks: BoosterBucks? = null
    ) : AchievementsUiState
    
    data class Failure(val message: String) : AchievementsUiState
}