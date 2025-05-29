package vc.prog3c.poe.data.models

data class Category(
    val id: String = "",
    val name: String = "",
    val type: CategoryType = CategoryType.CUSTOM,
    val icon: String = "",
    val color: String = "",
    val isEditable: Boolean = true
)

enum class CategoryType {
    SAVINGS,
    UTILITIES,
    EMERGENCY,
    CUSTOM
}

object PresetCategories {
    val SAVINGS = Category(
        id = "savings",
        name = "Savings",
        type = CategoryType.SAVINGS,
        icon = "ic_savings",
        color = "#4CAF50",
        isEditable = false
    )

    val UTILITIES = Category(
        id = "utilities",
        name = "Utilities",
        type = CategoryType.UTILITIES,
        icon = "ic_utilities",
        color = "#2196F3",
        isEditable = false
    )

    val EMERGENCY = Category(
        id = "emergency",
        name = "Emergency Fund",
        type = CategoryType.EMERGENCY,
        icon = "ic_emergency",
        color = "#F44336",
        isEditable = false
    )

    val allPresetCategories = listOf(SAVINGS, UTILITIES, EMERGENCY)
}