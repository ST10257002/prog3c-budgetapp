package vc.prog3c.poe.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import vc.prog3c.poe.data.models.Income

class IncomeRepository {

    private val db = FirebaseFirestore.getInstance()
    private val userId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    fun addIncome(income: Income, onComplete: (Boolean) -> Unit) {
        val uid = userId ?: return onComplete(false)

        db.collection("users")
            .document(uid)
            .collection("incomes")
            .document(income.id) // Use provided ID
            .set(income)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getAllIncomes(onComplete: (List<Income>) -> Unit) {
        val uid = userId ?: return onComplete(emptyList())

        db.collection("users")
            .document(uid)
            .collection("incomes")
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val incomes = result.mapNotNull { it.toObject(Income::class.java) }
                onComplete(incomes)
            }
            .addOnFailureListener {
                onComplete(emptyList())
            }
    }

    fun deleteIncome(incomeId: String, onComplete: (Boolean) -> Unit) {
        val uid = userId ?: return onComplete(false)

        db.collection("users")
            .document(uid)
            .collection("incomes")
            .document(incomeId)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun updateIncome(income: Income, onComplete: (Boolean) -> Unit) {
        val uid = userId ?: return onComplete(false)

        db.collection("users")
            .document(uid)
            .collection("incomes")
            .document(income.id)
            .set(income)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}
