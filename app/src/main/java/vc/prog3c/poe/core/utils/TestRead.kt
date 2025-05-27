package vc.prog3c.poe.utils

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import vc.prog3c.poe.data.models.Income

object TestRead {

    fun runIncomeTest() {
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: "testUser1234" // fallback for testing

        val income = Income(
            amount = 12500.0,
            source = "Software Dev Contract",
            date = Timestamp.now(),
            note = "Monthly income test"
        )

        // Insert the test data
        db.collection("users")
            .document(userId)
            .collection("incomes")
            .add(income)
            .addOnSuccessListener { docRef ->
                Log.d("TEST_READ", "Income inserted with ID: ${docRef.id}")

                // Read it back
                docRef.get()
                    .addOnSuccessListener { docSnapshot ->
                        val readIncome = docSnapshot.toObject(Income::class.java)
                        Log.d("TEST_READ", "Fetched income: $readIncome")
                    }
                    .addOnFailureListener { e ->
                        Log.e("TEST_READ", "Failed to read back inserted income", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("TEST_READ", "Failed to insert income", e)
            }
    }
}
