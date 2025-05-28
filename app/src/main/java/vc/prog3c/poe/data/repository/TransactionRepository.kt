package vc.prog3c.poe.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import vc.prog3c.poe.data.models.MonthlyStats
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import java.util.Calendar
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

    fun getMonthlyStats(year: Int, month: Int, onComplete: (MonthlyStats) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return onComplete(MonthlyStats(0.0, 0.0, 0.0))

        val db = FirebaseFirestore.getInstance()
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
        val startDate = com.google.firebase.Timestamp(startCal.time)
        val endDate = com.google.firebase.Timestamp(endCal.time)

        // First, get all accounts
        db.collection("users").document(userId).collection("accounts")
            .get()
            .addOnSuccessListener { accountSnapshot ->
                val accountIds = accountSnapshot.documents.map { it.id }
                var totalIncome = 0.0
                var totalExpenses = 0.0
                var completed = 0
                if (accountIds.isEmpty()) {
                    onComplete(MonthlyStats(0.0, 0.0, 0.0))
                    return@addOnSuccessListener
                }
                for (accountId in accountIds) {
                    db.collection("users").document(userId)
                        .collection("accounts").document(accountId)
                        .collection("transactions")
                        .whereGreaterThanOrEqualTo("date", startDate)
                        .whereLessThanOrEqualTo("date", endDate)
                        .get()
                        .addOnSuccessListener { txSnap ->
                            txSnap.documents.forEach { doc ->
                                val type = doc.getString("type")
                                val amount = doc.getDouble("amount") ?: 0.0
                                when (type) {
                                    "INCOME" -> totalIncome += amount
                                    "EXPENSE" -> totalExpenses += amount
                                }
                            }
                            completed++
                            if (completed == accountIds.size) {
                                val savings = totalIncome - totalExpenses
                                onComplete(MonthlyStats(totalIncome, totalExpenses, savings))
                            }
                        }
                }
            }
    }

    fun getExpenseCategoryBreakdown(
        year: Int,
        month: Int,
        onComplete: (Map<String, Double>) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return onComplete(emptyMap())

        // Pull all transactions for this month, filter to EXPENSE, group by category
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("accounts")
            .get()
            .addOnSuccessListener { accountsSnap ->
                val allAccountIds = accountsSnap.documents.map { it.id }
                val categoryTotals = mutableMapOf<String, Double>()

                // Get all transactions from all accounts (parallel)
                val tasks = allAccountIds.map { accountId ->
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .collection("accounts")
                        .document(accountId)
                        .collection("transactions")
                        .whereEqualTo("type", "EXPENSE")
                        .get()
                }

                com.google.android.gms.tasks.Tasks.whenAllSuccess<Any>(tasks)
                    .addOnSuccessListener { allResults ->
                        allResults.forEach { querySnapObj ->
                            val querySnap = querySnapObj as com.google.firebase.firestore.QuerySnapshot
                            for (doc in querySnap.documents) {
                                val transaction = doc.toObject(vc.prog3c.poe.data.models.Transaction::class.java)
                                transaction?.let {
                                    // Filter by date if needed (by month/year)
                                    val cal = java.util.Calendar.getInstance()
                                    it.date?.let { date ->
                                        cal.time = date.toDate()
                                        val txYear = cal.get(java.util.Calendar.YEAR)
                                        val txMonth = cal.get(java.util.Calendar.MONTH) + 1
                                        if (txYear == year && txMonth == month) {
                                            val cat = it.category ?: "Other"
                                            categoryTotals[cat] = (categoryTotals[cat] ?: 0.0) + it.amount
                                        }
                                    }
                                }
                            }
                        }
                        onComplete(categoryTotals)
                    }
                    .addOnFailureListener { onComplete(emptyMap()) }
            }
            .addOnFailureListener { onComplete(emptyMap()) }
    }

}
