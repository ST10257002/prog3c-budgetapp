package vc.prog3c.poe.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import java.util.Date

/**
 * Unified repository for both income and expense entries.
 * All transactions live under /users/{uid}/accounts/{accountId}/transactions/{transactionId}
 */
class TransactionRepository {

    private val db = FirebaseFirestore.getInstance()
    private val userId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    /**
     * Create a new transaction (income or expense).
     */
    fun addTransaction(
        transaction: Transaction,
        onComplete: (Boolean) -> Unit
    ) {
        val uid = userId ?: return onComplete(false)
        val accountId = transaction.accountId ?: return onComplete(false)

        db.collection("users")
            .document(uid)
            .collection("accounts")
            .document(accountId)
            .collection("transactions")
            .document(transaction.id)
            .set(transaction)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Fetch all transactions for an account, optionally filtering by type (INCOME/EXPENSE).
     */
    fun getTransactions(
        accountId: String,
        type: TransactionType? = null,
        onComplete: (List<Transaction>) -> Unit
    ) {
        val uid = userId ?: return onComplete(emptyList())

        var query = db.collection("users")
            .document(uid)
            .collection("accounts")
            .document(accountId)
            .collection("transactions")
            .orderBy("date", Query.Direction.DESCENDING)

        type?.let {
            query = query.whereEqualTo("type", it.name)
        }

        query.get()
            .addOnSuccessListener { snap ->
                val list = snap.documents
                    .mapNotNull { it.toObject(Transaction::class.java) }
                onComplete(list)
            }
            .addOnFailureListener {
                onComplete(emptyList())
            }
    }

    /**
     * Update an existing transaction.
     */
    fun updateTransaction(
        transaction: Transaction,
        onComplete: (Boolean) -> Unit
    ) {
        val uid = userId ?: return onComplete(false)
        val accountId = transaction.accountId ?: return onComplete(false)

        db.collection("users")
            .document(uid)
            .collection("accounts")
            .document(accountId)
            .collection("transactions")
            .document(transaction.id)
            .set(transaction)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Delete a transaction.
     */
    fun deleteTransaction(
        transactionId: String,
        accountId: String,
        onComplete: (Boolean) -> Unit
    ) {
        val uid = userId ?: return onComplete(false)

        db.collection("users")
            .document(uid)
            .collection("accounts")
            .document(accountId)
            .collection("transactions")
            .document(transactionId)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}
