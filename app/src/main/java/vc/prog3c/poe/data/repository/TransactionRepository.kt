package vc.prog3c.poe.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import vc.prog3c.poe.data.models.Account
import vc.prog3c.poe.data.models.Category
import vc.prog3c.poe.data.models.Transaction

class TransactionRepository {

    private val db = FirebaseFirestore.getInstance()
    private val userId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    fun addTransaction(transaction: Transaction, onComplete: (Boolean) -> Unit) {
        val uid = userId ?: return onComplete(false)
        val accountId = transaction.accountId ?: return onComplete(false)

        db.collection("users")
            .document(uid)
            .collection("accounts")
            .document(accountId)
            .collection("transactions")
            .document(transaction.id)
            .set(transaction)
            .addOnSuccessListener {
                Log.d("TX_UPSERT", "Transaction saved.")
                onComplete(true)
            }
            .addOnFailureListener {
                Log.e("TX_UPSERT", "Failed to save transaction: ${it.message}", it)
                onComplete(false)
            }
    }

    fun getAccount(accountId: String, onComplete: (Account?) -> Unit) {
        val uid = userId ?: return onComplete(null)

        db.collection("users")
            .document(uid)
            .collection("accounts")
            .document(accountId)
            .get()
            .addOnSuccessListener { doc ->
                val account = doc.toObject(Account::class.java)
                onComplete(account)
            }
            .addOnFailureListener {
                Log.e("TX_UPSERT", "Failed to load account: ${it.message}", it)
                onComplete(null)
            }
    }

    fun getAllCategories(onComplete: (List<Category>?) -> Unit) {
        val uid = userId ?: return onComplete(null)

        db.collection("users")
            .document(uid)
            .collection("categories")
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { it.toObject(Category::class.java) }
                onComplete(list)
            }
            .addOnFailureListener {
                Log.e("TX_UPSERT", "Failed to fetch categories: ${it.message}", it)
                onComplete(null)
            }
    }
}
