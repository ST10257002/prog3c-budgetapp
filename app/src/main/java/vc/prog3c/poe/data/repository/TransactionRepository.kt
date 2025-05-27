package vc.prog3c.poe.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType

class TransactionRepository {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    fun getAllTransactions(accountId: String?, onComplete: (List<Transaction>) -> Unit) {
        if (userId == null) {
            onComplete(emptyList())
            return
        }

        val expensesRef = db.collection("users").document(userId).collection("expenses")
        val incomesRef = db.collection("users").document(userId).collection("incomes")

        val expensesQuery = if (accountId != null)
            expensesRef.whereEqualTo("accountId", accountId)
        else
            expensesRef

        val incomesQuery = if (accountId != null)
            incomesRef.whereEqualTo("accountId", accountId)
        else
            incomesRef

        val allTransactions = mutableListOf<Transaction>()

        expensesQuery.get().addOnSuccessListener { expenseSnap ->
            val expenses = expenseSnap.mapNotNull { it.toTransaction(TransactionType.EXPENSE) }
            allTransactions.addAll(expenses)

            incomesQuery.get().addOnSuccessListener { incomeSnap ->
                val incomes = incomeSnap.mapNotNull { it.toTransaction(TransactionType.INCOME) }
                allTransactions.addAll(incomes)

                val sorted = allTransactions.sortedByDescending { it.date }
                onComplete(sorted)
            }.addOnFailureListener { onComplete(emptyList()) }
        }.addOnFailureListener { onComplete(emptyList()) }
    }

    private fun DocumentSnapshot.toTransaction(type: TransactionType): Transaction? {
        // don’t require a stored field; use the Firestore path
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return null

        val docAccountId = getString("accountId") ?: return null
        val amount       = getDouble("amount")       ?: return null
        val timestamp    = getTimestamp("date")?.toDate() ?: return null

        // grab the right field for category/source:
        val categoryOrSource = when(type) {
            TransactionType.INCOME  -> getString("source")
            TransactionType.EXPENSE -> getString("categoryId")
            else                    -> null
        } ?: return null

        return Transaction(
            id        = id,           // snapshot ID
            userId    = uid,          // path-based
            accountId = docAccountId,
            type      = type,
            amount    = amount,
            category  = categoryOrSource,
            description = getString("description"),
            date      = timestamp
        )
    }

}
