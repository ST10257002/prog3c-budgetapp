// SeedData.kt
package vc.prog3c.poe.core.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import vc.prog3c.poe.data.models.Account
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import com.google.firebase.Timestamp
import vc.prog3c.poe.data.models.SavingsGoal
import java.util.UUID
import java.util.Calendar
import vc.prog3c.poe.data.models.Budget

object SeedData {

    fun daysAgo(days: Int): Timestamp {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return Timestamp(cal.time)
    }

    fun seedTestData(userId: String) {
        val db = FirebaseFirestore.getInstance()

        val checkingId = UUID.randomUUID().toString()
        val savingsId  = UUID.randomUUID().toString()
        val ccId       = UUID.randomUUID().toString()

        val checkingTxs = listOf(
            Transaction(UUID.randomUUID().toString(), userId, checkingId, TransactionType.INCOME, 2500.0, "Income", daysAgo(28), "Payday!"),
            Transaction(UUID.randomUUID().toString(), userId, checkingId, TransactionType.EXPENSE, 120.0, "Utilities", daysAgo(16), "Monthly bill"),
            Transaction(UUID.randomUUID().toString(), userId, checkingId, TransactionType.EXPENSE, 15.0, "Expense", daysAgo(2), "Snacks")
        )

        val savingsTxs = listOf(
            Transaction(UUID.randomUUID().toString(), userId, savingsId, TransactionType.INCOME, 1000.0, "Savings", daysAgo(60), "Initial deposit"),
            Transaction(UUID.randomUUID().toString(), userId, savingsId, TransactionType.INCOME, 50.0, "Savings", daysAgo(29), "Interest"),
            Transaction(UUID.randomUUID().toString(), userId, savingsId, TransactionType.INCOME, 2000.0, "Savings", daysAgo(4), "Bonus")
        )

        val ccTxs = listOf(
            Transaction(UUID.randomUUID().toString(), userId, ccId, TransactionType.EXPENSE, 75.0, "Expense", daysAgo(90), "Online purchase"),
            Transaction(UUID.randomUUID().toString(), userId, ccId, TransactionType.EXPENSE, 30.0, "Expense", daysAgo(35), "Streaming"),
            Transaction(UUID.randomUUID().toString(), userId, ccId, TransactionType.EXPENSE, 45.0, "Emergency Fund", daysAgo(6), "Unexpected dinner")
        )

        val defaultCategories = listOf(
            Triple("Savings", 100.0, 500.0),
            Triple("Utilities", 50.0, 300.0),
            Triple("Emergency Fund", 75.0, 400.0),
            Triple("Income", 0.0, 0.0),
            Triple("Expense", 50.0, 200.0)
        )

        defaultCategories.forEach { (name, min, max) ->
            val catRef = db.collection("users").document(userId)

            catRef.collection("categoryBudgets")
                .document(name)
                .set(mapOf("minGoal" to min, "maxGoal" to max))

            catRef.collection("categories")
                .document(name)
                .set(mapOf(
                    "name" to name,
                    "minGoal" to min,
                    "maxGoal" to max,
                    "budgetLimit" to 0.0,
                    "type" to name.uppercase(),
                    "icon" to "",
                    "color" to "",
                    "isEditable" to true,
                    "description" to "$name category",
                    "isActive" to true,
                    "createdAt" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis()
                ))
        }

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, 3)

        val savingsGoals = defaultCategories.map { (name, _, _) ->
            SavingsGoal(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = name,
                targetAmount = 0.0,
                savedAmount = 0.0,
                targetDate = calendar.time,
                minMonthlyGoal = 100.0,
                maxMonthlyGoal = 500.0,
                monthlyBudget = 300.0
            )
        }

        savingsGoals.forEach { goal ->
            db.collection("users")
                .document(userId)
                .collection("savingsGoals")
                .document(goal.id)
                .set(goal)
        }

        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1

        val budgetId = String.format("%04d%02d", year, month)
        val monthlyBudget = Budget(budgetId, userId, 1500.0, 3000.0, 2500.0, month, year)

        db.collection("users")
            .document(userId)
            .collection("budgets")
            .document(budgetId)
            .set(monthlyBudget)

        fun calculateBalance(transactions: List<Transaction>): Double {
            return transactions.sumOf { tx ->
                when (tx.type) {
                    TransactionType.INCOME -> tx.amount
                    TransactionType.EXPENSE -> -tx.amount
                    TransactionType.EARNED, TransactionType.REDEEMED -> 0.0
                }
            }
        }

        val accounts = listOf(
            Account(checkingId, userId, "Everyday Checking", "Debit", calculateBalance(checkingTxs), checkingTxs.size),
            Account(savingsId, userId, "Rainy-Day Savings", "Savings", calculateBalance(savingsTxs), savingsTxs.size),
            Account(ccId, userId, "Visa Credit Card", "Credit", calculateBalance(ccTxs), ccTxs.size)
        )

        val txMap = mapOf(
            checkingId to checkingTxs,
            savingsId to savingsTxs,
            ccId to ccTxs
        )

        accounts.forEach { acct ->
            val acctRef = db.collection("users")
                .document(userId)
                .collection("accounts")
                .document(acct.id)

            acctRef.set(acct).addOnSuccessListener {
                txMap[acct.id]?.forEach { tx ->
                    acctRef.collection("transactions")
                        .document(tx.id)
                        .set(tx)
                }
            }
        }

        Log.d("Dashboard_Test", "Seed data completed")
    }
}
