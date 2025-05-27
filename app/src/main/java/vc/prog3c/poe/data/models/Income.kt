package vc.prog3c.poe.data.models

import com.google.firebase.Timestamp

data class Income(
    val id: String = "",
    val amount: Double = 0.0,
    val source: String = "",
    val date: Timestamp = Timestamp.now(),
    val description: String? = null,
    val accountId: String = "",
    val userId: String = ""
)
