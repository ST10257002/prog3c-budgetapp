package vc.prog3c.poe.data.models

data class Budget(
    val id: String = "",
    val userId: String = "",
    val min: Double = 0.0,
    val max: Double = 0.0,
    val target: Double = 0.0, // or just min and max
    val month: Int = 0, // e.g. 202405 for May 2024
    val year: Int = 0 // (optional, for querying)
)
