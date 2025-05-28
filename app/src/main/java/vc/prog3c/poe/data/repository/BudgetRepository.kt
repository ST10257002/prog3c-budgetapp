package vc.prog3c.poe.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import vc.prog3c.poe.data.models.Budget

class BudgetRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getBudgetForMonth(year: Int, month: Int, onComplete: (Budget?) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onComplete(null)
        val monthKey = String.format("%04d%02d", year, month) // e.g. 202405
        db.collection("users").document(userId)
            .collection("budgets")
            .document(monthKey)
            .get()
            .addOnSuccessListener { doc ->
                val budget = doc.toObject(Budget::class.java)
                onComplete(budget)
            }
            .addOnFailureListener { onComplete(null) }
    }

    fun saveBudget(budget: Budget, onComplete: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onComplete(false)
        val monthKey = String.format("%04d%02d", budget.year, budget.month)
        db.collection("users").document(userId)
            .collection("budgets")
            .document(monthKey)
            .set(budget)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}
