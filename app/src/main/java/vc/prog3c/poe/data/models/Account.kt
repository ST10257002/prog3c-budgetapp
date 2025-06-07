package vc.prog3c.poe.data.models

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Account(
    var id: String = "",
    var userId: String = "",
    var name: String = "",
    var type: String = "",
    var balance: Double = 0.0,
    var transactionsCount: Int = 0
)
