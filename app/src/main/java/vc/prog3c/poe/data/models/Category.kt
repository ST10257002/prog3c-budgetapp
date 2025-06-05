package vc.prog3c.poe.data.models

import java.util.UUID

data class Category(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val type: CategoryType = CategoryType.CUSTOM,
    val icon: String = "",
    val color: String = "",
    val isEditable: Boolean = true,
    val description: String = "",
    //val budgetLimit: Double = 0.0,
    val minBudget: Double = 0.0,
    val maxBudget: Double = 0.0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class CategoryType {
    SAVINGS,
    UTILITIES,
    EMERGENCY,
    INCOME,
    EXPENSE,
    CUSTOM;

    companion object {
        fun fromString(value: String): CategoryType {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                CUSTOM
            }
        }
    }
}
