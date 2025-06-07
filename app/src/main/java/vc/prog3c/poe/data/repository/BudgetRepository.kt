package vc.prog3c.poe.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import vc.prog3c.poe.data.models.Budget
import vc.prog3c.poe.data.models.MonthlyStats
import java.util.Calendar

class BudgetRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getBudgetForMonth(year: Int, month: Int, onComplete: (Budget?) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onComplete(null)
        val monthKey = String.format("%04d%02d", year, month) // e.g. 202405
        db.collection("users").document(userId)
            .collection("budgets")
            .document(monthKey)
            .get()
            .addOnSuccessListener { doc ->
                val budget = doc.toObject(Budget::class.java)
                onComplete(budget)
            }
            .addOnFailureListener { onComplete(null) }
    }

    fun saveBudget(budget: Budget, onComplete: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onComplete(false)
        val monthKey = String.format("%04d%02d", budget.year, budget.month)
        db.collection("users").document(userId)
            .collection("budgets")
            .document(monthKey)
            .set(budget)
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
