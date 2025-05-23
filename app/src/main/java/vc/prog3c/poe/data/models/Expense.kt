package vc.prog3c.poe.data.models

data class Expense(
    val amount: Double = 0.0,
    val categoryId: String = "",
    val accountId: String = "",
    val date: String = "", //Timestamp?
    val description: String = "",
    val imageUrl: String? = null
)