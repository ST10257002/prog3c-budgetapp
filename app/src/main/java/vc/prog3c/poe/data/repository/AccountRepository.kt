package vc.prog3c.poe.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import vc.prog3c.poe.data.models.Account
import vc.prog3c.poe.data.models.Transaction

class AccountRepository {
    private val db = FirebaseFirestore.getInstance()
    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    suspend fun getAllAccounts(): Result<List<Account>> = runCatching {
        val accountsSnapshot = db.collection("users")
            .document(userId)
            .collection("accounts")
            .orderBy("name")
            .get()
            .await()

        val accounts = accountsSnapshot.toObjects(Account::class.java)

        accounts.forEach { account ->
            val txSnap = db.collection("users")
                .document(userId)
                .collection("accounts")
                .document(account.id)
                .collection("transactions")
                .get()
                .await()

            val txs = txSnap.toObjects(Transaction::class.java)
            account.transactionsCount = txs.size
            account.balance = txs.sumOf { it.amount }
        }

        accounts
    }

    suspend fun getAccount(accountId: String): Result<Account?> = runCatching {
        val doc = db.collection("users")
            .document(userId)
            .collection("accounts")
            .document(accountId)
            .get()
            .await()

        doc.toObject(Account::class.java)
    }

    suspend fun getTransactionsForAccount(accountId: String): Result<List<Transaction>> = runCatching {
        val txSnap = db.collection("users")
            .document(userId)
            .collection("accounts")
            .document(accountId)
            .collection("transactions")
            .orderBy("date", Query.Direction.ASCENDING)
            .get()
            .await()

        txSnap.toObjects(Transaction::class.java)
    }

    suspend fun addAccount(account: Account): Result<Unit> = runCatching {
        db.collection("users")
            .document(userId)
            .collection("accounts")
            .document(account.id)
            .set(account)
            .await()
    }

    suspend fun addOrUpdateAccount(account: Account): Result<Unit> = runCatching {
        db.collection("users")
            .document(userId)
            .collection("accounts")
            .document(account.id)
            .set(account)
            .await()
    }

    suspend fun deleteAccount(accountId: String): Result<Unit> = runCatching {
        db.collection("users")
            .document(userId)
            .collection("accounts")
            .document(accountId)
            .delete()
            .await()
    }
}
