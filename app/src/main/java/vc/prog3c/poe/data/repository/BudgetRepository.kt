package vc.prog3c.poe.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import vc.prog3c.poe.data.models.Budget

class BudgetRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val userId: String
        get() = auth.currentUser?.uid ?: ""

    suspend fun getBudgetForMonth(year: Int, month: Int): Result<Budget?> = runCatching {
        val monthKey = String.format("%04d%02d", year, month)
        val doc = db.collection("users")
            .document(userId)
            .collection("budgets")
            .document(monthKey)
            .get()
            .await()
        doc.toObject(Budget::class.java)
    }

    suspend fun saveBudget(budget: Budget): Result<Unit> = runCatching {
        val monthKey = String.format("%04d%02d", budget.year, budget.month)
        db.collection("users")
            .document(userId)
            .collection("budgets")
            .document(monthKey)
            .set(budget)
            .await()
    }
}
