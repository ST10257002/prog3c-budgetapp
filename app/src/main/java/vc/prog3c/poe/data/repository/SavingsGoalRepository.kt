package vc.prog3c.poe.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import vc.prog3c.poe.data.models.SavingsGoal

class SavingsGoalRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "Dashboard_Test"
    }

    // Save a new savings goal
    fun saveGoal(goal: SavingsGoal, onComplete: (Boolean) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "saveGoal -> User is null")
            onComplete(false)
            return
        }
        val userId = currentUser.uid
        val goalWithUserId = goal.copy(userId = userId)

        try {
            db.collection("users")
                .document(userId)
                .collection("savingsGoals")
                .add(goalWithUserId)
                .addOnSuccessListener { documentRef ->
                    Log.d(TAG, "saveGoal -> Success, goalId=${documentRef.id}")
                    documentRef.update("id", documentRef.id)
                    onComplete(true)
                }
                .addOnFailureListener {
                    Log.e(TAG, "saveGoal -> Failed: ${it.message}", it)
                    onComplete(false)
                }
        } catch (e: Exception) {
            Log.e(TAG, "saveGoal -> Exception: ${e.message}", e)
            onComplete(false)
        }
    }

    // Update an existing savings goal
    fun updateGoal(goalId: String, updatedFields: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "updateGoal -> User is null")
            onComplete(false)
            return
        }
        val userId = currentUser.uid

        try {
            db.collection("users")
                .document(userId)
                .collection("savingsGoals")
                .document(goalId)
                .update(updatedFields)
                .addOnSuccessListener {
                    Log.d(TAG, "updateGoal -> Success for $goalId")
                    onComplete(true)
                }
                .addOnFailureListener {
                    Log.e(TAG, "updateGoal -> Failed: ${it.message}", it)
                    onComplete(false)
                }
        } catch (e: Exception) {
            Log.e(TAG, "updateGoal -> Exception: ${e.message}", e)
            onComplete(false)
        }
    }

    // Fetch all savings goals for the current user
    fun fetchGoals(onComplete: (List<SavingsGoal>) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "fetchGoals -> User is null")
            onComplete(emptyList())
            return
        }
        val userId = currentUser.uid

        try {
            db.collection("users")
                .document(userId)
                .collection("savingsGoals")
                .get()
                .addOnSuccessListener { snapshot ->
                    val goals = snapshot.documents.mapNotNull { it.toObject(SavingsGoal::class.java) }
                    Log.d(TAG, "fetchGoals -> Retrieved ${goals.size} goals")
                    onComplete(goals)
                }
                .addOnFailureListener {
                    Log.e(TAG, "fetchGoals -> Failed: ${it.message}", it)
                    onComplete(emptyList())
                }
        } catch (e: Exception) {
            Log.e(TAG, "fetchGoals -> Exception: ${e.message}", e)
            onComplete(emptyList())
        }
    }
}
