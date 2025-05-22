package vc.prog3c.poe.data.models

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val address: String = "",
    val phoneNumber: String = "",
    val profilePictureUrl: String = "",
    val cardDetails: CardDetails = CardDetails()
) 