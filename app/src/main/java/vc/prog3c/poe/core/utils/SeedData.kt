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
            Transaction(UUID.randomUUID().toString(), userId, checkingId, TransactionType.INCOME, 2500.0, "Salary deposit", daysAgo(28), "Payday!"),
            Transaction(UUID.randomUUID().toString(), userId, checkingId, TransactionType.EXPENSE, 120.0, "Electric bill", daysAgo(16), "Monthly bill"),
            Transaction(UUID.randomUUID().toString(), userId, checkingId, TransactionType.EXPENSE, 15.0, "Coffee shop", daysAgo(2), "Morning coffee")
        )

        val savingsTxs = listOf(
            Transaction(UUID.randomUUID().toString(), userId, savingsId, TransactionType.INCOME, 1000.0, "Transfer from checking", daysAgo(60), "Saving up"),
            Transaction(UUID.randomUUID().toString(), userId, savingsId, TransactionType.INCOME, 50.0, "Monthly interest", daysAgo(29), "Interest payment"),
            Transaction(UUID.randomUUID().toString(), userId, savingsId, TransactionType.INCOME, 2000.0, "Gran's gift", daysAgo(4), "Gift from gran")
        )

        val ccTxs = listOf(
            Transaction(UUID.randomUUID().toString(), userId, ccId, TransactionType.EXPENSE, 75.0, "Online purchase", daysAgo(90), "Bought some stuff"),
            Transaction(UUID.randomUUID().toString(), userId, ccId, TransactionType.EXPENSE, 30.0, "Streaming subscription", daysAgo(35), "Netflix"),
            Transaction(UUID.randomUUID().toString(), userId, ccId, TransactionType.EXPENSE, 45.0, "Restaurant dinner", daysAgo(6), "Dinner with friends")
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
            Category(
                id = UUID.randomUUID().toString(),
                name = "Savings",
                type = CategoryType.SAVINGS,
                icon = "ic_savings",
                color = "#4CAF50",
                isEditable = true,
                description = "Regular savings account",
                minBudget = 500.0,
                maxBudget = 2000.0
            ),
            Category(
                id = UUID.randomUUID().toString(),
                name = "Utilities",
                type = CategoryType.UTILITIES,
                icon = "ic_utilities",
                color = "#2196F3",
                isEditable = true,
                description = "Monthly utility bills",
                minBudget = 800.0,
                maxBudget = 1500.0
            ),
            Category(
                id = UUID.randomUUID().toString(),
                name = "Emergency Fund",
                type = CategoryType.EMERGENCY,
                icon = "ic_emergency",
                color = "#F44336",
                isEditable = true,
                description = "Emergency fund for unexpected expenses",
                minBudget = 300.0,
                maxBudget = 1000.0
            ),
            Category(
                id = UUID.randomUUID().toString(),
                name = "Income",
                type = CategoryType.INCOME,
                icon = "ic_income",
                color = "#4CAF50",
                isEditable = true,
                description = "Regular income sources",
                minBudget = 0.0,
                maxBudget = 0.0
            ),
            Category(
                id = UUID.randomUUID().toString(),
                name = "Expense",
                type = CategoryType.EXPENSE,
                icon = "ic_expense",
                color = "#F44336",
                isEditable = true,
                description = "General expense tracking",
                minBudget = 0.0,
                maxBudget = 0.0
            )
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
