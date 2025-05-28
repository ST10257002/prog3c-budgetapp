package vc.prog3c.poe.core.utils

import com.google.firebase.firestore.FirebaseFirestore
import vc.prog3c.poe.data.models.Account
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import com.google.firebase.Timestamp
import java.util.UUID
import java.util.Calendar

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

        // Define transactions for each account with spread-out dates
        val checkingTxs = listOf(
            Transaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                accountId = checkingId,
                type = TransactionType.INCOME,
                amount = 2500.0,
                category = "Salary deposit",
                date = daysAgo(28), // 4 weeks ago
                description = "Payday!"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                accountId = checkingId,
                type = TransactionType.EXPENSE,
                amount = 120.0,
                category = "Electric bill",
                date = daysAgo(16), // just over 2 weeks ago
                description = "Monthly bill"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                accountId = checkingId,
                type = TransactionType.EXPENSE,
                amount = 15.0,
                category = "Coffee shop",
                date = daysAgo(2), // 2 days ago
                description = "Morning coffee"
            )
        )

        val savingsTxs = listOf(
            Transaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                accountId = savingsId,
                type = TransactionType.INCOME,
                amount = 1000.0,
                category = "Transfer from checking",
                date = daysAgo(60), // ~2 months ago
                description = "Saving up"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                accountId = savingsId,
                type = TransactionType.INCOME,
                amount = 50.0,
                category = "Monthly interest",
                date = daysAgo(29), // 1 month ago
                description = "Interest payment"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                accountId = savingsId,
                type = TransactionType.INCOME,
                amount = 2000.0,
                category = "Gran’s gift",
                date = daysAgo(4), // recent
                description = "Gift from gran"
            )
        )

        val ccTxs = listOf(
            Transaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                accountId = ccId,
                type = TransactionType.EXPENSE,
                amount = 75.0,
                category = "Online purchase",
                date = daysAgo(90), // 3 months ago
                description = "Bought some stuff"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                accountId = ccId,
                type = TransactionType.EXPENSE,
                amount = 30.0,
                category = "Streaming subscription",
                date = daysAgo(35), // just over a month ago
                description = "Netflix"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                accountId = ccId,
                type = TransactionType.EXPENSE,
                amount = 45.0,
                category = "Restaurant dinner",
                date = daysAgo(6), // almost a week ago
                description = "Dinner with friends"
            )
        )

        // Helper function to calculate balance from transaction list
        fun calcBalance(transactions: List<Transaction>): Double {
            return transactions.fold(0.0) { acc, tx ->
                when (tx.type) {
                    TransactionType.INCOME  -> acc + tx.amount
                    TransactionType.EXPENSE -> acc - tx.amount
                }
            }
        }

        // Create account objects with dynamic balances
        val accounts = listOf(
            Account(checkingId, userId, "Everyday Checking", "Debit",   calcBalance(checkingTxs), checkingTxs.size),
            Account(savingsId,  userId, "Rainy-Day Savings","Savings", calcBalance(savingsTxs),   savingsTxs.size),
            Account(ccId,       userId, "Visa Credit Card", "Credit",  calcBalance(ccTxs),        ccTxs.size)
        )

        val txMap = mapOf(
            checkingId to checkingTxs,
            savingsId to savingsTxs,
            ccId to ccTxs
        )

        // Write each account and its transactions
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
    }
}
