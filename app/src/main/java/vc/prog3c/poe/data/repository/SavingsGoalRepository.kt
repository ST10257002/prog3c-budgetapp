package vc.prog3c.poe.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import vc.prog3c.poe.data.models.SavingsGoal

class SavingsGoalRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun saveGoal(goal: SavingsGoal, onComplete: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onComplete(false)

        // Firestore path: users/{userId}/goals/savings_goal
        db.collection("users")
            .document(userId)
            .collection("goals")
            .document("savings_goal") // singular goal per user
            .set(goal.copy(userId = userId)) // ensure correct UID is saved
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun fetchGoal(onComplete: (SavingsGoal?) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onComplete(null)

        db.collection("users")
            .document(userId)
            .collection("goals")
            .document("savings_goal")
            .get()
            .addOnSuccessListener { snapshot ->
                val goal = snapshot.toObject(SavingsGoal::class.java)
                onComplete(goal)
            }
            .addOnFailureListener { onComplete(null) }
    }
}
