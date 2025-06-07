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
import java.util.Date

class AchievementViewModel(
    private val authService: AuthService = AuthService()
) : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _achievements = MutableLiveData<List<Achievement>>()
    val achievements: LiveData<List<Achievement>> = _achievements

    private val _boosterBucks = MutableLiveData<BoosterBucks>()
    val boosterBucks: LiveData<BoosterBucks> = _boosterBucks

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadAchievements()
        loadBoosterBucks()
    }

    private fun loadAchievements() {
        viewModelScope.launch {
            try {
                val userId = authService.getCurrentUser()?.uid ?: return@launch
                val userAchievements =
                    db.collection("users").document(userId).collection("achievements").get().await()
                        .toObjects(Achievement::class.java)

                // Merge with default achievements
                val allAchievements =
                    AchievementDefinitions.achievements.map { defaultAchievement ->
                        userAchievements.find { it.id == defaultAchievement.id }
                            ?.let { userAchievement ->
                                defaultAchievement.copy(
                                    isCompleted = userAchievement.isCompleted,
                                    completedAt = userAchievement.completedAt,
                                    progress = userAchievement.progress
                                )
                            } ?: defaultAchievement
                    }

                _achievements.value = allAchievements
            } catch (e: Exception) {
                _error.value = "Failed to load achievements: ${e.message}"
            }
        }
    }

    private fun loadBoosterBucks() {
        viewModelScope.launch {
            try {
                val userId = authService.getCurrentUser()?.uid ?: return@launch
                val boosterBucksRef =
                    db.collection("users").document(userId).collection("boosterBucks")
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

                _boosterBucks.value = boosterBucks
            } catch (e: Exception) {
                _error.value = "Failed to load Booster Bucks: ${e.message}"
            }
        }
    }

    fun redeemBoosterBucks() {
        val currentBoosterBucks = _boosterBucks.value ?: return
        if (currentBoosterBucks.availableBalance < BoosterBucks.MIN_REDEMPTION) {
            _error.value = "Insufficient Booster Bucks balance"
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
                db.collection("users").document(currentBoosterBucks.userId)
                    .collection("boosterBucks").document("balance").set(updatedBoosterBucks).await()

                // Record transaction
                val transaction = BoosterBucksTransaction(
                    userId = currentBoosterBucks.userId,
                    amount = redeemedAmount,
                    type = TransactionType.REDEEMED,
                    description = "Redeemed Booster Bucks",
                    timestamp = Date()
                )

                db.collection("boosterBucksTransactions").add(transaction).await()

                _boosterBucks.value = updatedBoosterBucks
            } catch (e: Exception) {
                _error.value = "Failed to redeem Booster Bucks: ${e.message}"
            }
        }
    }

    fun updateAchievementProgress(userId: String, achievementId: String, progress: Int) {
        viewModelScope.launch {
            try {
                val currentAchievements = _achievements.value ?: return@launch
                val achievement =
                    currentAchievements.find { it.id == achievementId } ?: return@launch

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

                db.collection("users").document(userId).collection("achievements")
                    .document(achievementId).set(achievementData).await()

                // If achievement was just completed, award Booster Bucks
                if (updatedAchievement.isCompleted && !achievement.isCompleted) {
                    val currentBoosterBucks = _boosterBucks.value ?: return@launch
                    val updatedBoosterBucks = currentBoosterBucks.copy(
                        totalEarned = currentBoosterBucks.totalEarned + updatedAchievement.boosterBucksReward,
                        currentBalance = currentBoosterBucks.currentBalance + updatedAchievement.boosterBucksReward,
                        lastUpdated = Date()
                    )

                    db.collection("users").document(userId).collection("boosterBucks")
                        .document("balance").set(updatedBoosterBucks).await()

                    _boosterBucks.value = updatedBoosterBucks
                }

                // Update local achievements list
                _achievements.value = currentAchievements.map {
                    if (it.id == achievementId) updatedAchievement else it
                }
            } catch (e: Exception) {
                _error.value = "Failed to update achievement progress: ${e.message}"
            }
        }
    }
} 