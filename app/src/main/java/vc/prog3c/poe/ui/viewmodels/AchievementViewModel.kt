package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import vc.prog3c.poe.core.services.AuthService
import vc.prog3c.poe.data.models.Achievement
import vc.prog3c.poe.data.models.AchievementDefinitions
import vc.prog3c.poe.data.models.BoosterBucks
import vc.prog3c.poe.data.models.BoosterBucksTransaction
import vc.prog3c.poe.data.models.TransactionType
import vc.prog3c.poe.ui.viewmodels.AchievementsUiState.Failure
import vc.prog3c.poe.ui.viewmodels.AchievementsUiState.Updated
import java.util.Date

class AchievementViewModel(
    private val authService: AuthService = AuthService(),
    private val dataService: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {
    companion object {
        private const val TAG = "AchievementViewModel"
    }


    // --- Fields


    private val _uiState = MutableLiveData<AchievementsUiState>()
    val uiState: LiveData<AchievementsUiState> = _uiState
    
    
    private var _achievements: MutableList<Achievement>? = null
    private var _boosterBucks: BoosterBucks? = null


    init {
        loadAchievements()
        loadBoosterBucks()
    }


    // --- Internals


    private fun loadAchievements() = viewModelScope.launch {
        try {
            val userId = authService.getCurrentUser()?.uid ?: return@launch
            val userAchievements =
                dataService.collection("users").document(userId).collection("achievements").get()
                    .await().toObjects(Achievement::class.java)

            // Merge with default achievements
            val allAchievements = AchievementDefinitions.achievements.map { defaultAchievement ->
                userAchievements.find { it.id == defaultAchievement.id }?.let { userAchievement ->
                        defaultAchievement.copy(
                            isCompleted = userAchievement.isCompleted,
                            completedAt = userAchievement.completedAt,
                            progress = userAchievement.progress
                        )
                    } ?: defaultAchievement
            }
            
            _achievements = allAchievements.toMutableList()
            _uiState.value = Updated(
                achievements = _achievements
            )
        } catch (e: Exception) {
            _uiState.value = Failure("Failed to load achievements: ${e.message}")
        }
    }


    private fun loadBoosterBucks() = viewModelScope.launch {
        try {
            val userId = authService.getCurrentUser()?.uid ?: return@launch
            val boosterBucksRef =
                dataService.collection("users").document(userId).collection("boosterBucks")
                    .document("balance")

            val snapshot = boosterBucksRef.get().await()
            val boosterBucks = if (snapshot.exists()) {
                BoosterBucks(
                    userId = userId,
                    totalEarned = (snapshot.getLong("totalEarned") ?: 0).toInt(),
                    totalRedeemed = (snapshot.getLong("totalRedeemed") ?: 0).toInt(),
                    currentBalance = (snapshot.getLong("currentBalance") ?: 0).toInt(),
                    lastUpdated = snapshot.getTimestamp("lastUpdated")?.toDate() ?: Date()
                )
            } else {
                BoosterBucks(
                    userId = userId,
                    totalEarned = 0,
                    totalRedeemed = 0,
                    currentBalance = 0,
                    lastUpdated = Date()
                )
            }
            
            _boosterBucks = boosterBucks
            _uiState.value = Updated(
                boosterBucks = _boosterBucks
            )
        } catch (e: Exception) {
            _uiState.value = Failure("Failed to load Booster Bucks: ${e.message}")
        }
    }


    fun redeemBoosterBucks() {
        val currentBoosterBucks = _boosterBucks ?: return
        if (currentBoosterBucks.availableBalance < BoosterBucks.MIN_REDEMPTION) {
            _uiState.value = Failure("Insufficient Booster Bucks balance")
            return
        }

        viewModelScope.launch {
            try {
                val redeemedAmount = currentBoosterBucks.availableBalance
                val updatedBoosterBucks = currentBoosterBucks.copy(
                    totalRedeemed = currentBoosterBucks.totalRedeemed + redeemedAmount,
                    currentBalance = 0,
                    lastUpdated = Date()
                )

                // Update Booster Bucks
                dataService.collection("users").document(currentBoosterBucks.userId)
                    .collection("boosterBucks").document("balance").set(updatedBoosterBucks).await()

                // Record transaction
                val transaction = BoosterBucksTransaction(
                    userId = currentBoosterBucks.userId,
                    amount = redeemedAmount,
                    type = TransactionType.REDEEMED,
                    description = "Redeemed Booster Bucks",
                    timestamp = Date()
                )

                dataService.collection("boosterBucksTransactions").add(transaction).await()

                _uiState.value = Updated(
                    boosterBucks = updatedBoosterBucks
                )
            } catch (e: Exception) {
                _uiState.value = Failure("Failed to redeem Booster Bucks: ${e.message}")
            }
        }
    }


    fun updateAchievementProgress(
        userId: String, achievementId: String, progress: Int
    ) = viewModelScope.launch {
        try {
            val currentAchievements = _achievements ?: return@launch
            val achievement = currentAchievements.find { it.id == achievementId } ?: return@launch

            val updatedAchievement = achievement.copy(
                progress = progress,
                isCompleted = progress >= achievement.requiredProgress,
                completedAt = if (progress >= achievement.requiredProgress) Timestamp(Date()) else null
            )

            // Update achievement in Firestore
            val achievementData = hashMapOf(
                "progress" to progress,
                "isCompleted" to (progress >= achievement.requiredProgress),
                "completedAt" to (if (progress >= achievement.requiredProgress) Timestamp(Date()) else null)
            )

            dataService.collection("users").document(userId).collection("achievements")
                .document(achievementId).set(achievementData).await()

            // If achievement was just completed, award Booster Bucks
            if (updatedAchievement.isCompleted && !achievement.isCompleted) {
                val currentBoosterBucks = _boosterBucks ?: return@launch
                val updatedBoosterBucks = currentBoosterBucks.copy(
                    totalEarned = currentBoosterBucks.totalEarned + updatedAchievement.boosterBucksReward,
                    currentBalance = currentBoosterBucks.currentBalance + updatedAchievement.boosterBucksReward,
                    lastUpdated = Date()
                )

                dataService.collection("users").document(userId).collection("boosterBucks")
                    .document("balance").set(updatedBoosterBucks).await()

                _boosterBucks = updatedBoosterBucks
                _uiState.value = Updated(
                    boosterBucks = _boosterBucks
                )
            }

            // Update local achievements list
            _achievements = currentAchievements.map {
                if (it.id == achievementId) updatedAchievement else it
            }.toMutableList()
            _uiState.value = Updated(
                achievements = _achievements
            )
        } catch (e: Exception) {
            _uiState.value = Failure("Failed to update achievement progress: ${e.message}")
        }
    }
} 