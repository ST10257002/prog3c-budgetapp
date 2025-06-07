package vc.prog3c.poe.data.models

import java.util.Date

data class Goal(
    val id: String,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val deadline: Date,
    val category: String,
    val description: String? = null,
    val isCompleted: Boolean = false
)