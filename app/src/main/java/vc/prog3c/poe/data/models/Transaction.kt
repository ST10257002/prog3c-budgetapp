package vc.prog3c.poe.data.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

/**
 * Represents a single transaction (income or expense) under an account.
 */

@IgnoreExtraProperties
@Parcelize
data class Transaction(
    val id: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val amount: Double = 0.0,
    val description: String = "",
    val date: Timestamp = Timestamp.now(),
    val category: String = "",
    val accountId: String = "",
    val userId: String = "",
    val photoUrls: List<String> = emptyList()
) : Parcelable {
    // Empty constructor for Firestore
    constructor() : this(
        id = "",
        type = TransactionType.EXPENSE,
        amount = 0.0,
        description = "",
        date = Timestamp.now(),
        category = "",
        accountId = "",
        userId = "",
        photoUrls = emptyList()
    )
}


