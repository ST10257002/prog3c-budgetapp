package vc.prog3c.poe.data.models

import com.google.firebase.Timestamp

data class Expense(
    val id: String = "",
    val userId: String = "",            // ← new
    val amount: Double = 0.0,
    val categoryId: String = "",
    val accountId: String = "",
    val date: Timestamp = Timestamp.now(),
    val description: String = "",
    val imageUrl: String? = null
)
