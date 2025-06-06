package vc.prog3c.poe.core.utils

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import vc.prog3c.poe.data.models.*
import java.util.Calendar
import java.util.UUID

object SeedData {

    private fun daysAgo(days: Int): Timestamp {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return Timestamp(cal.time)
    }

    private fun calculateBalance(transactions: List<Transaction>): Double {
        return transactions.sumOf { tx ->
            when (tx.type) {
                TransactionType.INCOME   -> tx.amount
                TransactionType.EXPENSE  -> -tx.amount
                TransactionType.EARNED,
                TransactionType.REDEEMED -> 0.0
            }
        }
    }

    fun seedTestData(userId: String) {
        val db = FirebaseFirestore.getInstance()

        // Generate account IDs
        val checkingId = UUID.randomUUID().toString()
        val savingsId  = UUID.randomUUID().toString()
        val ccId       = UUID.randomUUID().toString()

        // -------------------------------
        // Transactions
        // -------------------------------

        val checkingTxs = listOf(
            Transaction(UUID.randomUUID().toString(), userId, checkingId, TransactionType.INCOME, 3200.0, "Income", daysAgo(27), "Monthly salary"),
            Transaction(UUID.randomUUID().toString(), userId, checkingId, TransactionType.EXPENSE, 100.0, "Utilities", daysAgo(20), "Water bill"),
            Transaction(UUID.randomUUID().toString(), userId, checkingId, TransactionType.EXPENSE, 75.0, "Groceries", daysAgo(14), "Grocery store purchase"),
            Transaction(UUID.randomUUID().toString(), userId, checkingId, TransactionType.EXPENSE, 45.0, "Expense", daysAgo(10), "Streaming subscription (Netflix & Spotify)"),
            Transaction(UUID.randomUUID().toString(), userId, checkingId, TransactionType.EXPENSE, 60.0, "Transport", daysAgo(5), "Fuel for car"),
            Transaction(UUID.randomUUID().toString(), userId, checkingId, TransactionType.EXPENSE, 25.0, "Utilities", daysAgo(3), "Mobile data recharge")
        )

        val savingsTxs = listOf(
            Transaction(UUID.randomUUID().toString(), userId, savingsId, TransactionType.INCOME, 1000.0, "Savings", daysAgo(25), "Transfer from checking account"),
            Transaction(UUID.randomUUID().toString(), userId, savingsId, TransactionType.INCOME, 50.0, "Savings", daysAgo(18), "Monthly bank interest"),
            Transaction(UUID.randomUUID().toString(), userId, savingsId, TransactionType.INCOME, 200.0, "Income", daysAgo(15), "Freelance job payout"),
            Transaction(UUID.randomUUID().toString(), userId, savingsId, TransactionType.EXPENSE, 100.0, "Emergency", daysAgo(12), "Emergency car tire replacement"),
            Transaction(UUID.randomUUID().toString(), userId, savingsId, TransactionType.INCOME, 500.0, "Income", daysAgo(9), "Annual bonus received"),
            Transaction(UUID.randomUUID().toString(), userId, savingsId, TransactionType.INCOME, 300.0, "Income", daysAgo(6), "Gift from family")
        )

        val ccTxs = listOf(
            Transaction(UUID.randomUUID().toString(), userId, ccId, TransactionType.EXPENSE, 150.0, "Expense", daysAgo(30), "New headphones purchase"),
            Transaction(UUID.randomUUID().toString(), userId, ccId, TransactionType.EXPENSE, 40.0, "Expense", daysAgo(24), "Online course payment"),
            Transaction(UUID.randomUUID().toString(), userId, ccId, TransactionType.EXPENSE, 70.0, "Groceries", daysAgo(16), "Dinner at restaurant"),
            Transaction(UUID.randomUUID().toString(), userId, ccId, TransactionType.EXPENSE, 35.0, "Emergency", daysAgo(13), "Pharmacy visit – medicine & vitamins"),
            Transaction(UUID.randomUUID().toString(), userId, ccId, TransactionType.EXPENSE, 60.0, "Expense", daysAgo(7), "Gym membership fee"),
            Transaction(UUID.randomUUID().toString(), userId, ccId, TransactionType.EXPENSE, 95.0, "Expense", daysAgo(2), "New shoes purchase")
        )

        // -------------------------------
        // Savings Goal
        // -------------------------------

        val goalId = UUID.randomUUID().toString()
        val calendar = Calendar.getInstance().apply { add(Calendar.MONTH, 3) }

        val savingsGoal = SavingsGoal(
            id = goalId,
            userId = userId,
            name = "Holiday Trip",
            targetAmount = 5000.0,
            savedAmount = 1250.0,
            targetDate = calendar.time,
            minMonthlyGoal = 1000.0,
            maxMonthlyGoal = 2000.0,
            monthlyBudget = 1500.0
        )

        db.collection("users")
            .document(userId)
            .collection("savingsGoals")
            .document(goalId)
            .set(savingsGoal)

        // -------------------------------
        // Monthly Budget
        // -------------------------------

        val now = Calendar.getInstance()
        val year = now.get(Calendar.YEAR)
        val month = now.get(Calendar.MONTH) + 1 // 0-based

        val budgetId = String.format("%04d%02d", year, month)

        val monthlyBudget = Budget(
            id = budgetId,
            userId = userId,
            min = 1500.0,
            max = 3000.0,
            target = 2500.0,
            month = month,
            year = year
        )

        db.collection("users")
            .document(userId)
            .collection("budgets")
            .document(budgetId)
            .set(monthlyBudget)

        // -------------------------------
        // Accounts + Transactions
        // -------------------------------

        val accounts = listOf(
            Account(checkingId, userId, "Everyday Checking", "Debit", calculateBalance(checkingTxs), checkingTxs.size),
            Account(savingsId,  userId, "Rainy-Day Savings", "Savings", calculateBalance(savingsTxs), savingsTxs.size),
            Account(ccId,       userId, "Visa Credit Card",  "Credit",  calculateBalance(ccTxs), ccTxs.size)
        )

        val txMap = mapOf(
            checkingId to checkingTxs,
            savingsId  to savingsTxs,
            ccId       to ccTxs
        )

        accounts.forEach { account ->
            val acctRef = db.collection("users")
                .document(userId)
                .collection("accounts")
                .document(account.id)

            acctRef.set(account).addOnSuccessListener {
                txMap[account.id]?.forEach { tx ->
                    acctRef.collection("transactions")
                        .document(tx.id)
                        .set(tx)
                }
            }
        }

        // -------------------------------
        // Seed Categories as user data
        // -------------------------------

        val seededCategories = listOf(
            Category(UUID.randomUUID().toString(), "Savings", CategoryType.SAVINGS, "ic_savings", "#4CAF50", true, "Savings", 500.0, 2000.0),
            Category(UUID.randomUUID().toString(), "Utilities", CategoryType.UTILITIES, "ic_utilities", "#2196F3", true, "Utilities", 800.0, 1500.0),
            Category(UUID.randomUUID().toString(), "Emergency", CategoryType.EMERGENCY, "ic_emergency", "#F44336", true, "Emergency", 300.0, 1000.0),
            Category(UUID.randomUUID().toString(), "Income", CategoryType.INCOME, "ic_income", "#4CAF50", true, "Income", 0.0, 0.0),
            Category(UUID.randomUUID().toString(), "Expense", CategoryType.EXPENSE, "ic_expense", "#F44336", true, "Expense", 0.0, 0.0),
            Category(UUID.randomUUID().toString(), "Groceries", CategoryType.EXPENSE, "ic_expense", "#FFA726", true, "Food and groceries", 500.0, 1200.0),
            Category(UUID.randomUUID().toString(), "Transport", CategoryType.EXPENSE, "ic_expense", "#607D8B", true, "Fuel, Uber, etc", 200.0, 800.0)
        )

        seededCategories.forEach { category ->
            db.collection("users")
                .document(userId)
                .collection("categories")
                .document(category.id)
                .set(category)
        }
    }
}
