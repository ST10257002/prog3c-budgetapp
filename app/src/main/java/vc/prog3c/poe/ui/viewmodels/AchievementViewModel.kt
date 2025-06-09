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
import vc.prog3c.poe.data.models.BoosterBucks
import vc.prog3c.poe.data.models.BoosterBucksTransaction
import vc.prog3c.poe.data.models.TransactionType
import java.util.Date
import vc.prog3c.poe.core.utils.Event
import vc.prog3c.poe.data.models.AchievementCategory
/**
 * @reference Firebase Firestore - Update Documents: https://firebase.google.com/docs/firestore/manage-data/add-data#update-data
 * @reference Firebase Firestore - Subcollections and Merging Data: https://firebase.google.com/docs/firestore/manage-data/structure-data
 * @reference Firebase Firestore - Kotlin Await: https://firebase.google.com/docs/firestore/query-data/get-data#kotlin+ktx
 */

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

    private val _newlyCompleted = MutableLiveData<Event<Achievement>>()
    val newlyCompleted: LiveData<Event<Achievement>> = _newlyCompleted

    init {
        loadAchievements()
        loadBoosterBucks()
    }

    private fun loadAchievements() {
        viewModelScope.launch {
            try {
                val userId = authService.getCurrentUser()?.uid ?: return@launch

                // Load master definitions
                val masterSnapshot = db.collection("achievements").get().await()
                val definitions = masterSnapshot.documents.mapNotNull { doc ->
                    Achievement(
                        id = doc.getString("id") ?: doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        category = AchievementCategory.valueOf(doc.getString("category") ?: "USER_MILESTONES"),
                        boosterBucksReward = doc.getLong("boosterBucksReward")?.toInt() ?: 0,
                        requiredProgress = doc.getLong("requiredProgress")?.toInt() ?: 1
                    )
                }

                // Load user progress
                val userSnapshot = db.collection("users")
                    .document(userId)
                    .collection("achievements")
                    .get().await()

                val userData = userSnapshot.documents.associateBy { it.id }

                // Merge
                val merged = definitions.map { definition ->
                    val progressDoc = userData[definition.id]
                    definition.copy(
                        progress = progressDoc?.getLong("progress")?.toInt() ?: 0,
                        isCompleted = progressDoc?.getBoolean("isCompleted") ?: false,
                        completedAt = progressDoc?.getTimestamp("completedAt")
                    )
                }

                _achievements.value = merged

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

                val isNewlyCompleted = progress >= achievement.requiredProgress && !achievement.isCompleted

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

                // Award Booster Bucks and emit unlock event if newly completed
                if (isNewlyCompleted) {
                    val currentBoosterBucks = _boosterBucks.value ?: return@launch
                    val updatedBoosterBucks = currentBoosterBucks.copy(
                        totalEarned = currentBoosterBucks.totalEarned + updatedAchievement.boosterBucksReward,
                        currentBalance = currentBoosterBucks.currentBalance + updatedAchievement.boosterBucksReward,
                        lastUpdated = Date()
                    )

                    db.collection("users").document(userId).collection("boosterBucks")
                        .document("balance").set(updatedBoosterBucks).await()

                    _boosterBucks.value = updatedBoosterBucks
                    _newlyCompleted.value = Event(updatedAchievement) // 🔥 only emit once, here
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