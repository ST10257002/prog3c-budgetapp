package vc.prog3c.poe.data.models

data class Card(
    val id: String,
    val type: String,
    val balance: Double,
    val cardNumber: String,
    val expiryDate: String
)