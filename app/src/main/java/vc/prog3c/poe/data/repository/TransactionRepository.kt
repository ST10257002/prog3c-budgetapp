package vc.prog3c.poe.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import vc.prog3c.poe.data.models.MonthlyStats
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import java.util.*

class TransactionRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val userId: String
        get() = auth.currentUser?.uid ?: ""

    private fun transactionCollection(accountId: String) =
        db.collection("users")
            .document(userId)
            .collection("accounts")
            .document(accountId)
            .collection("transactions")

    suspend fun addTransaction(transaction: Transaction): Result<Unit> = runCatching {
        val accountId = transaction.accountId.ifEmpty { throw Exception("Missing accountId") }
        transactionCollection(accountId)
            .document(transaction.id)
            .set(transaction)
            .await()
    }

    suspend fun updateTransaction(transaction: Transaction): Result<Unit> = addTransaction(transaction)

    suspend fun deleteTransaction(accountId: String, transactionId: String): Result<Unit> = runCatching {
        transactionCollection(accountId)
            .document(transactionId)
            .delete()
            .await()
    }

    suspend fun getTransactions(accountId: String, type: TransactionType? = null): Result<List<Transaction>> = runCatching {
        var query: Query = transactionCollection(accountId).orderBy("date", Query.Direction.DESCENDING)
        if (type != null) query = query.whereEqualTo("type", type.name)

        val snapshot = query.get().await()
        snapshot.documents.mapNotNull { it.toObject(Transaction::class.java) }
    }

    suspend fun getMonthlyStats(year: Int, month: Int): Result<MonthlyStats> = runCatching {
        val startCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, startCal.getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val startDate = Timestamp(startCal.time)
        val endDate = Timestamp(endCal.time)

        val accountSnapshot = db.collection("users")
            .document(userId)
            .collection("accounts")
            .get().await()

        val accountIds = accountSnapshot.documents.map { it.id }
        var totalIncome = 0.0
        var totalExpenses = 0.0

        for (accountId in accountIds) {
            val transactions = transactionCollection(accountId)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .get().await()

            for (doc in transactions) {
                val type = doc.getString("type")
                val amount = doc.getDouble("amount") ?: 0.0
                when (type) {
                    "INCOME" -> totalIncome += amount
                    "EXPENSE" -> totalExpenses += amount
                }
            }
        }

        MonthlyStats(totalIncome, totalExpenses, totalIncome - totalExpenses)
    }

    suspend fun getExpenseCategoryBreakdown(year: Int, month: Int): Result<Map<String, Double>> = runCatching {
        val accountSnapshot = db.collection("users")
            .document(userId)
            .collection("accounts")
            .get().await()

        val categoryTotals = mutableMapOf<String, Double>()

        for (accountDoc in accountSnapshot) {
            val transactions = transactionCollection(accountDoc.id)
                .whereEqualTo("type", "EXPENSE")
                .get().await()

            for (doc in transactions) {
                val transaction = doc.toObject(Transaction::class.java) ?: continue
                val cal = Calendar.getInstance().apply { time = transaction.date.toDate() }
                val txYear = cal.get(Calendar.YEAR)
                val txMonth = cal.get(Calendar.MONTH) + 1
                if (txYear == year && txMonth == month) {
                    val category = transaction.category.ifBlank { "Other" }
                    categoryTotals[category] = (categoryTotals[category] ?: 0.0) + transaction.amount
                }
            }
        }

        categoryTotals
    }

    suspend fun getCategoryTotals(): Result<Map<String, Double>> = runCatching {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val result = mutableMapOf<String, Double>()

        val accountDocs = db.collection("users").document(userId).collection("accounts").get().await()
        for (account in accountDocs) {
            val txns = db.collection("users")
                .document(userId)
                .collection("accounts")
                .document(account.id)
                .collection("transactions")
                .whereEqualTo("type", "EXPENSE")
                .get()
                .await()

            for (doc in txns) {
                val category = doc.getString("category") ?: "Other"
                val amount = doc.getDouble("amount") ?: 0.0
                result[category] = result.getOrDefault(category, 0.0) + amount
            }
        }
        result
    }

}
