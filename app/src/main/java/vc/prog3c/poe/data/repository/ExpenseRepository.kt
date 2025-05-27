// @reference: https://firebase.google.com/docs/firestore/manage-data/add-data

package vc.prog3c.poe.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import vc.prog3c.poe.data.models.Expense

class ExpenseRepository {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    fun addExpense(expense: Expense, onComplete: (Boolean) -> Unit) {
        if (userId == null) {
            onComplete(false)
            return
        }

        db.collection("users")
            .document(userId)
            .collection("expenses")
            .add(expense)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getAllExpenses(onComplete: (List<Expense>) -> Unit) {
        if (userId == null) return

        db.collection("users")
            .document(userId)
            .collection("expenses")
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val expenses = result.mapNotNull { it.toObject(Expense::class.java) }
                onComplete(expenses)
            }
    }

    fun updateExpense(expenseId: String, updatedExpense: Expense, onComplete: (Boolean) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return onComplete(false)

        db.collection("users")
            .document(userId)
            .collection("expenses")
            .document(expenseId)
            .set(updatedExpense)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun deleteExpense(expenseId: String, onComplete: (Boolean) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return onComplete(false)

        db.collection("users")
            .document(userId)
            .collection("expenses")
            .document(expenseId)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

}