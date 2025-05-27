package vc.prog3c.poe.data.models

import java.util.Date

data class SavingsGoal(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val targetAmount: Double = 0.0,
    val savedAmount: Double = 0.0,
    val targetDate: Date? = null
) 