package vc.prog3c.poe.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import vc.prog3c.poe.data.models.SavingsGoal

class SavingsGoalRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val userId: String
        get() = auth.currentUser?.uid ?: ""

    suspend fun fetchGoals(): Result<List<SavingsGoal>> = runCatching {
        val snapshot = db.collection("users")
            .document(userId)
            .collection("savingsGoals")
            .get()
            .await()
        snapshot.documents.mapNotNull { it.toObject(SavingsGoal::class.java) }
    }

    suspend fun saveGoal(goal: SavingsGoal): Result<Unit> = runCatching {
        val docRef = db.collection("users")
            .document(userId)
            .collection("savingsGoals")
            .document() // Auto-ID
        val goalWithId = goal.copy(id = docRef.id)
        docRef.set(goalWithId).await()
    }

    suspend fun updateGoal(goalId: String, fields: Map<String, Any>): Result<Unit> = runCatching {
        db.collection("users")
            .document(userId)
            .collection("savingsGoals")
            .document(goalId)
            .update(fields)
            .await()
    }
}
