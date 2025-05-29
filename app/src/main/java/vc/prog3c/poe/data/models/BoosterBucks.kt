package vc.prog3c.poe.data.models

import java.util.Date

data class BoosterBucks(
    val userId: String = "",
    val totalEarned: Int = 0,
    val totalRedeemed: Int = 0,
    val currentBalance: Int = 0,
    val lastUpdated: Date = Date()
) {
    val availableBalance: Int
        get() = currentBalance

    companion object {
        const val CONVERSION_RATE = 0.04 // 250 Booster Bucks = R10
        const val MIN_REDEMPTION = 250 // Minimum Booster Bucks needed for redemption
    }
}

data class BoosterBucksTransaction(
    val id: String = "",
    val userId: String = "",
    val amount: Int = 0,
    val type: TransactionType = TransactionType.EARNED,
    val description: String = "",
    val timestamp: Date = Date()
) 