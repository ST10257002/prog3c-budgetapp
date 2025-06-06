package vc.prog3c.poe.data.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Represents a single transaction (income or expense) under an account.
 */

@IgnoreExtraProperties
data class Transaction(
    var id: String = "",
    var userId: String = "",
    var accountId: String = "",
    var type: TransactionType = TransactionType.EXPENSE,
    var amount: Double = 0.0,
    var category: String = "",
    val date: Timestamp = Timestamp.now(),
    var description: String? = null,
    var photoUrls: List<String> = emptyList()
)


