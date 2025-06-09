package vc.prog3c.poe.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import vc.prog3c.poe.data.models.SavingsGoal
/**
 * @reference Firebase Firestore - Auto-ID Documents: https://firebase.google.com/docs/firestore/manage-data/add-data#add_a_document
 * @reference Firebase Firestore - Update a Document: https://firebase.google.com/docs/firestore/manage-data/add-data#update-data
 */

class SavingsGoalRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Get the current savings goal
    fun getCurrentGoal(onComplete: (SavingsGoal?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onComplete(null)
            return
        }
        val userId = currentUser.uid

        db.collection("users")
            .document(userId)
            .collection("savingsGoals")
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                val goal = snapshot.documents.firstOrNull()?.toObject(SavingsGoal::class.java)
                onComplete(goal)
            }
            .addOnFailureListener { onComplete(null) }
    }

    // Save a new savings goal
    fun saveGoal(goal: SavingsGoal, onComplete: (Boolean) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onComplete(false)
            return
        }
        val userId = currentUser.uid
        val goalWithUserId = goal.copy(userId = userId)

        // Use an auto-generated ID for each new savings goal
        db.collection("users")
            .document(userId)
            .collection("savingsGoals")
            .add(goalWithUserId) // Adds a new document with an auto-generated ID
            .addOnSuccessListener { documentRef ->
                // Optionally update the goal with its ID
                documentRef.update("id", documentRef.id)
                onComplete(true)
            }
            .addOnFailureListener { onComplete(false) }
    }

    // Update an existing savings goal
    fun updateGoal(goalId: String, updatedFields: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onComplete(false)
            return
        }
        val userId = currentUser.uid

        db.collection("users")
            .document(userId)
            .collection("savingsGoals")
            .document(goalId)
            .update(updatedFields)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // Fetch all savings goals for the current user
    fun fetchGoals(onComplete: (List<SavingsGoal>) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onComplete(emptyList())
            return
        }
        val userId = currentUser.uid

        db.collection("users")
            .document(userId)
            .collection("savingsGoals")
            .get()
            .addOnSuccessListener { snapshot ->
                val goals = snapshot.documents.mapNotNull { it.toObject(SavingsGoal::class.java) }
                onComplete(goals)
            }
            .addOnFailureListener { onComplete(emptyList()) }
    }
}