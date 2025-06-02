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
    val budgetLimit: Double = 0.0,
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

object PresetCategories {
    val SAVINGS = Category(
        id = "savings",
        name = "Savings",
        type = CategoryType.SAVINGS,
        icon = "ic_savings",
        color = "#4CAF50",
        isEditable = false,
        description = "Regular savings account"
    )

    val UTILITIES = Category(
        id = "utilities",
        name = "Utilities",
        type = CategoryType.UTILITIES,
        icon = "ic_utilities",
        color = "#2196F3",
        isEditable = false,
        description = "Monthly utility bills"
    )

    val EMERGENCY = Category(
        id = "emergency",
        name = "Emergency Fund",
        type = CategoryType.EMERGENCY,
        icon = "ic_emergency",
        color = "#F44336",
        isEditable = false,
        description = "Emergency fund for unexpected expenses"
    )

    val INCOME = Category(
        id = "income",
        name = "Income",
        type = CategoryType.INCOME,
        icon = "ic_income",
        color = "#4CAF50",
        isEditable = false,
        description = "Regular income sources"
    )

    val EXPENSE = Category(
        id = "expense",
        name = "Expense",
        type = CategoryType.EXPENSE,
        icon = "ic_expense",
        color = "#F44336",
        isEditable = false,
        description = "Regular expenses"
    )

    val allPresetCategories = listOf(SAVINGS, UTILITIES, EMERGENCY, INCOME, EXPENSE)

    fun getCategoryByType(type: CategoryType): Category? {
        return allPresetCategories.find { it.type == type }
    }
}