package vc.prog3c.poe.data.models

data class Account(
    val id: String,
    val userId: String,
    val name: String,
    val type: String,
    val balance: Double,
    val transactionsCount: Int = 0
) 