package vc.prog3c.poe.data.models

import java.util.Date

data class Transaction(
    val id: String,
    val userId: String,
    val accountId: String? = null,
    val type: TransactionType,
    val amount: Double,
    val category: String,
    val date: Date,
    val description: String? = null
)

enum class TransactionType {
    ALL,
    INCOME,
    EXPENSE
} 