package vc.prog3c.poe.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import vc.prog3c.poe.data.models.Budget
import vc.prog3c.poe.data.models.MonthlyStats
import java.util.*

class BudgetRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getBudgetForMonth(year: Int, month: Int, callback: (Budget) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(Budget())
            return
        }
        val userId = currentUser.uid

        db.collection("users")
            .document(userId)
            .collection("budgets")
            .document("${year}_${month}")
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val budget = document.toObject(Budget::class.java)
                    callback(budget ?: Budget())
                } else {
                    callback(Budget())
                }
            }
            .addOnFailureListener {
                callback(Budget())
            }
    }

    fun getMonthlyStats(year: Int, month: Int, callback: (MonthlyStats) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(MonthlyStats())
            return
        }
        val userId = currentUser.uid

        // Get all transactions for the month
        db.collection("users").document(userId).collection("accounts").get()
            .addOnSuccessListener { accountDocs ->
                val accountIds = accountDocs.map { it.id }
                var totalExpenses = 0.0
                val tasks = accountIds.map { accountId ->
                    db.collection("users").document(userId).collection("accounts")
                        .document(accountId).collection("transactions")
                        .whereEqualTo("type", "EXPENSE")
                        .get()
                }

                com.google.android.gms.tasks.Tasks.whenAllSuccess<Any>(tasks)
                    .addOnSuccessListener { results ->
                        for ((index, res) in results.withIndex()) {
                            val snap = res as com.google.firebase.firestore.QuerySnapshot
                            for (doc in snap.documents) {
                                val amount = doc.getDouble("amount") ?: 0.0
                                totalExpenses += amount
                            }
                        }

                        // Get the user's set budget
                        db.collection("users").document(userId)
                            .collection("budgets")
                            .document("${year}_${month}")
                            .get()
                            .addOnSuccessListener { budgetDoc ->
                                val budget = budgetDoc.getDouble("target") ?: 0.0
                                
                                callback(MonthlyStats(
                                    totalExpenses = totalExpenses,
                                    budget = budget
                                ))
                            }
                            .addOnFailureListener {
                                callback(MonthlyStats(
                                    totalExpenses = totalExpenses,
                                    budget = 0.0
                                ))
                            }
                    }
                    .addOnFailureListener {
                        callback(MonthlyStats())
                    }
            }
    }

    fun updateBudget(budget: Budget, callback: (Boolean) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(false)
            return
        }
        val userId = currentUser.uid

        val cal = java.util.Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1

        db.collection("users")
            .document(userId)
            .collection("budgets")
            .document("${year}_${month}")
            .set(budget)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
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
