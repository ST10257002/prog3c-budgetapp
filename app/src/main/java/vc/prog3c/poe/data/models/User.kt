package vc.prog3c.poe.data.models

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val totalBalance: Double = 0.0,
    val profilePictureUrl: String
) 