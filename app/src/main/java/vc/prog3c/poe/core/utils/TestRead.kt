package vc.prog3c.poe.utils

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import java.util.UUID

object TestRead {

    fun runTransactionTest() {
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: "testUser1234" // fallback for testing

        val testAccountId = "testAccount123" // Or generate/test with a real account
        val transactionId = UUID.randomUUID().toString()
        val transaction = Transaction(
            id = transactionId,
            userId = userId,
            accountId = testAccountId,
            type = TransactionType.INCOME,
            amount = 12500.0,
            category = "Software Dev Contract",
            date = Timestamp.now(),
            description = "Monthly income test"
        )

        db.collection("users")
            .document(userId)
            .collection("transactions") // Use your app's collection structure
            .document(transactionId)
            .set(transaction)
            .addOnSuccessListener {
                Log.d("TEST_READ", "Transaction inserted with ID: $transactionId")

                // Read it back
                db.collection("users")
                    .document(userId)
                    .collection("transactions")
                    .document(transactionId)
                    .get()
                    .addOnSuccessListener { docSnapshot ->
                        val readTx = docSnapshot.toObject(Transaction::class.java)
                        Log.d("TEST_READ", "Fetched transaction: $readTx")
                    }
                    .addOnFailureListener { e ->
                        Log.e("TEST_READ", "Failed to read back inserted transaction", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("TEST_READ", "Failed to insert transaction", e)
            }
    }
}
