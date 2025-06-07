package vc.prog3c.poe.data.repository

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import vc.prog3c.poe.data.models.Account
import vc.prog3c.poe.data.models.Transaction

class AccountRepository {
    private val db = FirebaseFirestore.getInstance()
    private val uid: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    // pull in aggregates via getAccountsWithDetails
    fun getAllAccounts(onComplete: (List<Account>) -> Unit) {
        val userId = uid ?: run {
            Log.e("AccountRepository", "User not authenticated")
            return onComplete(emptyList())
        }
        Log.d("AccountRepository", "Fetching accounts for user: $userId")
        getAccountsWithDetails(userId, onComplete)
    }

    // fetch a single account doc by ID
    fun getAccount(
        accountId: String,
        onComplete: (Account?) -> Unit
    ) {
        val userId = uid ?: return onComplete(null)
        db.collection("users")
            .document(userId)
            .collection("accounts")
            .document(accountId)
            .get()
            .addOnSuccessListener { snap ->
                onComplete(snap.toObject(Account::class.java))
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    // fetch all transactions under one account
    fun getTransactionsForAccount(
        accountId: String,
        onComplete: (List<Transaction>) -> Unit
    ) {
        val userId = uid ?: return onComplete(emptyList())
        db.collection("users")
            .document(userId)
            .collection("accounts")
            .document(accountId)
            .collection("transactions")
            .orderBy("date", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snap ->
                val txs = snap.documents.mapNotNull { it.toObject(Transaction::class.java) }
                onComplete(txs)
            }
            .addOnFailureListener {
                onComplete(emptyList())
            }
    }

    fun addAccount(account: Account, onComplete: (Boolean) -> Unit) {
        val userId = uid ?: return onComplete(false)
        db.collection("users")
            .document(userId)
            .collection("accounts")
            .document(account.id)
            .set(account)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun updateAccount(account: Account, onComplete: (Boolean) -> Unit) {
        // same as addAccount since set() will overwrite
        addAccount(account, onComplete)
    }

    fun deleteAccount(accountId: String, onComplete: (Boolean) -> Unit) {
        val userId = uid ?: return onComplete(false)
        db.collection("users")
            .document(userId)
            .collection("accounts")
            .document(accountId)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getAccountsWithDetails(userId: String, onComplete: (List<Account>) -> Unit) {
        val accountsRef = db.collection("users")
            .document(userId)
            .collection("accounts")

        Log.d("AccountRepository", "Querying accounts collection")
        accountsRef.orderBy("name")
            .get()
            .addOnSuccessListener { snap ->
                val accounts = snap.documents.mapNotNull { it.toObject(Account::class.java) }
                Log.d("AccountRepository", "Found ${accounts.size} accounts")

                // For each account, fetch its transactions sub-collection
                val detailTasks = accounts.map { account ->
                    accountsRef
                        .document(account.id)
                        .collection("transactions")
                        .get()
                        .continueWith { task ->
                            val txDocs = task.result?.documents ?: emptyList()
                            // count & sum
                            account.transactionsCount = txDocs.size
                            account.balance = txDocs.sumOf { doc ->
                                // make sure your field name matches; default to 0.0
                                doc.getDouble("amount") ?: 0.0
                            }
                            Log.d("AccountRepository", "Account ${account.name}: ${account.transactionsCount} transactions, balance ${account.balance}")
                        }
                }

                // When *all* per-account tasks complete…
                Tasks.whenAll(detailTasks)
                    .addOnSuccessListener { 
                        Log.d("AccountRepository", "Successfully loaded all account details")
                        onComplete(accounts) 
                    }
                    .addOnFailureListener {
                        Log.e("AccountRepository", "Failed to load account details", it)
                        onComplete(accounts)
                    }
            }
            .addOnFailureListener {
                Log.e("AccountRepository", "Failed to load accounts", it)
                onComplete(emptyList())
            }
    }

}
