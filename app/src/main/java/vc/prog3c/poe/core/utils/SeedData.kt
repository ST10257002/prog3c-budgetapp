package vc.prog3c.poe.core.utils

import com.google.firebase.firestore.FirebaseFirestore
import vc.prog3c.poe.data.models.Account
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import com.google.firebase.Timestamp
import java.util.UUID
object SeedData {
    fun seedTestData(userId: String) {
        val db = FirebaseFirestore.getInstance()

        val checkingId = UUID.randomUUID().toString()
        val savingsId  = UUID.randomUUID().toString()
        val ccId       = UUID.randomUUID().toString()

        // Define transactions for each account
        val checkingTxs = listOf(
            Transaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                accountId = checkingId,
                type = TransactionType.INCOME,
                amount = 2500.0,
                category = "Salary deposit",
                date = Timestamp.now(),
                description = "Payday!"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                accountId = checkingId,
                type = TransactionType.EXPENSE,
                amount = 120.0,
                category = "Electric bill",
                date = Timestamp.now(),
                description = "Monthly bill"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                accountId = checkingId,
                type = TransactionType.EXPENSE,
                amount = 15.0,
                category = "Coffee shop",
                date = Timestamp.now(),
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
                date = Timestamp.now(),
                description = "Saving up"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                accountId = savingsId,
                type = TransactionType.INCOME,
                amount = 50.0,
                category = "Monthly interest",
                date = Timestamp.now(),
                description = "Interest payment"
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
                date = Timestamp.now(),
                description = "Bought some stuff"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                accountId = ccId,
                type = TransactionType.EXPENSE,
                amount = 30.0,
                category = "Streaming subscription",
                date = Timestamp.now(),
                description = "Netflix"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                accountId = ccId,
                type = TransactionType.EXPENSE,
                amount = 45.0,
                category = "Restaurant dinner",
                date = Timestamp.now(),
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

