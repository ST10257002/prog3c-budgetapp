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

        val accounts = listOf(
            Account(checkingId, userId, "Everyday Checking", "Debit",   1200.0, 0),
            Account(savingsId,  userId, "Rainy-Day Savings","Savings", 5000.0, 0),
            Account(ccId,       userId, "Visa Credit Card", "Credit",  -250.0, 0)
        )

        accounts.forEach { acct ->
            val acctRef = db.collection("users")
                .document(userId)
                .collection("accounts")
                .document(acct.id)

            // Write the account
            acctRef.set(acct).addOnSuccessListener {
                val txs = when (acct.id) {
                    checkingId -> listOf(
                        Transaction(
                            id        = UUID.randomUUID().toString(),
                            userId    = userId,
                            accountId = acct.id,
                            type      = TransactionType.INCOME,
                            amount    = 2500.0,
                            category  = "Salary deposit",
                            date      = Timestamp.now(),
                            description = "Payday!"
                        ),
                        Transaction(
                            id        = UUID.randomUUID().toString(),
                            userId    = userId,
                            accountId = acct.id,
                            type      = TransactionType.EXPENSE,
                            amount    = 120.0,
                            category  = "Electric bill",
                            date      = Timestamp.now(),
                            description = "Monthly bill"
                        ),
                        Transaction(
                            id        = UUID.randomUUID().toString(),
                            userId    = userId,
                            accountId = acct.id,
                            type      = TransactionType.EXPENSE,
                            amount    = 15.0,
                            category  = "Coffee shop",
                            date      = Timestamp.now(),
                            description = "Morning coffee"
                        )
                    )

                    savingsId -> listOf(
                        Transaction(
                            id        = UUID.randomUUID().toString(),
                            userId    = userId,
                            accountId = acct.id,
                            type      = TransactionType.INCOME,
                            amount    = 1000.0,
                            category  = "Transfer from checking",
                            date      = Timestamp.now(),
                            description = "Saving up"
                        ),
                        Transaction(
                            id        = UUID.randomUUID().toString(),
                            userId    = userId,
                            accountId = acct.id,
                            type      = TransactionType.INCOME,
                            amount    = 50.0,
                            category  = "Monthly interest",
                            date      = Timestamp.now(),
                            description = "Interest payment"
                        )
                    )

                    ccId -> listOf(
                        Transaction(
                            id        = UUID.randomUUID().toString(),
                            userId    = userId,
                            accountId = acct.id,
                            type      = TransactionType.EXPENSE,
                            amount    = 75.0,
                            category  = "Online purchase",
                            date      = Timestamp.now(),
                            description = "Bought some stuff"
                        ),
                        Transaction(
                            id        = UUID.randomUUID().toString(),
                            userId    = userId,
                            accountId = acct.id,
                            type      = TransactionType.EXPENSE,
                            amount    = 30.0,
                            category  = "Streaming subscription",
                            date      = Timestamp.now(),
                            description = "Netflix"
                        ),
                        Transaction(
                            id        = UUID.randomUUID().toString(),
                            userId    = userId,
                            accountId = acct.id,
                            type      = TransactionType.EXPENSE,
                            amount    = 45.0,
                            category  = "Restaurant dinner",
                            date      = Timestamp.now(),
                            description = "Dinner with friends"
                        )
                    )
                    else -> emptyList()
                }

                // Write transactions to a "transactions" subcollection under the account
                txs.forEach { tx ->
                    acctRef.collection("transactions")
                        .document(tx.id)
                        .set(tx)
                }
            }
        }
    }
}
