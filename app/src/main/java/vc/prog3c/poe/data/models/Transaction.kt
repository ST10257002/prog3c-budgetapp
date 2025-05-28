package vc.prog3c.poe.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Transaction(
    var id: String = "",
    var userId: String = "",
    var accountId: String = "",
    var type: TransactionType = TransactionType.EXPENSE,
    var amount: Double = 0.0,
    var category: String = "",
    var date: Timestamp = Timestamp.now(),
    var description: String? = null
)
