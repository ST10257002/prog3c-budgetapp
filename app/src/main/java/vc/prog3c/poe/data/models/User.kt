package vc.prog3c.poe.data.models

data class User(
    val id: String = "", // Firestore doc ID = Firebase UID
    val uid: String = "",         // FirebaseAuth UID
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val totalBalance: Double = 0.0,
    val profilePictureUrl: String = "",
    val cardNumber: String = "",
    val cardType: String = "",  // e.g., Visa, Mastercard
    val cvc: String = "",
    val expiryDate: String = "" // e.g., 12/28
) {
    fun getFullName(): String = "$name $surname"
}
