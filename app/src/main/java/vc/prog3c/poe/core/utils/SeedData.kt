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
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.INCOME,
                amount = 3200.0,
                description = "Monthly salary",
                date = daysAgo(27),
                category = "Income",
                accountId = checkingId,
                userId = userId
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                amount = 100.0,
                description = "Water bill",
                date = daysAgo(20),
                category = "Utilities",
                accountId = checkingId,
                userId = userId
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                amount = 75.0,
                description = "Grocery store purchase",
                date = daysAgo(14),
                category = "Groceries",
                accountId = checkingId,
                userId = userId
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                amount = 45.0,
                description = "Streaming subscription (Netflix & Spotify)",
                date = daysAgo(10),
                category = "Expense",
                accountId = checkingId,
                userId = userId
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                amount = 60.0,
                description = "Fuel for car",
                date = daysAgo(5),
                category = "Transport",
                accountId = checkingId,
                userId = userId
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                amount = 25.0,
                description = "Mobile data recharge",
                date = daysAgo(3),
                category = "Utilities",
                accountId = checkingId,
                userId = userId
            )
        )

        val savingsTxs = listOf(
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.INCOME,
                amount = 1000.0,
                description = "Transfer from checking account",
                date = daysAgo(25),
                category = "Savings",
                accountId = savingsId,
                userId = userId
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.INCOME,
                amount = 50.0,
                description = "Monthly bank interest",
                date = daysAgo(18),
                category = "Savings",
                accountId = savingsId,
                userId = userId
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.INCOME,
                amount = 200.0,
                description = "Freelance job payout",
                date = daysAgo(15),
                category = "Income",
                accountId = savingsId,
                userId = userId
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                amount = 100.0,
                description = "Emergency car tire replacement",
                date = daysAgo(12),
                category = "Emergency",
                accountId = savingsId,
                userId = userId
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.INCOME,
                amount = 500.0,
                description = "Annual bonus received",
                date = daysAgo(9),
                category = "Income",
                accountId = savingsId,
                userId = userId
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.INCOME,
                amount = 300.0,
                description = "Gift from family",
                date = daysAgo(6),
                category = "Income",
                accountId = savingsId,
                userId = userId
            )
        )

        val ccTxs = listOf(
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                amount = 150.0,
                description = "New headphones purchase",
                date = daysAgo(30),
                category = "Expense",
                accountId = ccId,
                userId = userId
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                amount = 40.0,
                description = "Online course payment",
                date = daysAgo(24),
                category = "Expense",
                accountId = ccId,
                userId = userId
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                amount = 70.0,
                description = "Dinner at restaurant",
                date = daysAgo(16),
                category = "Groceries",
                accountId = ccId,
                userId = userId
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                amount = 35.0,
                description = "Pharmacy visit – medicine & vitamins",
                date = daysAgo(13),
                category = "Emergency",
                accountId = ccId,
                userId = userId
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                amount = 60.0,
                description = "Gym membership fee",
                date = daysAgo(7),
                category = "Expense",
                accountId = ccId,
                userId = userId
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                amount = 95.0,
                description = "New shoes purchase",
                date = daysAgo(2),
                category = "Expense",
                accountId = ccId,
                userId = userId
            )
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

            // Needed to be run once
//        // -------------------------------
//        // Seed Global Master Achievements
//        // -------------------------------
//
//        val masterAchievements = AchievementDefinitions.achievements
//        val achievementBatch = db.batch()
//
//        masterAchievements.forEach { achievement ->
//            val achievementData = mapOf(
//                "id" to achievement.id,
//                "title" to achievement.title,
//                "description" to achievement.description,
//                "category" to achievement.category.name,
//                "boosterBucksReward" to achievement.boosterBucksReward,
//                "requiredProgress" to achievement.requiredProgress
//            )
//
//            val ref = db.collection("achievements").document(achievement.id)
//            achievementBatch.set(ref, achievementData)
//        }
//
//        achievementBatch.commit()

    }
}
