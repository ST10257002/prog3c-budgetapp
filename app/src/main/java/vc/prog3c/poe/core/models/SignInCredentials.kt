package vc.prog3c.poe.core.models

import vc.prog3c.poe.core.utils.AuthValidator

/**
 * Data transfer object for authenticating a user.
 */
data class SignInCredentials(
    val identity: String, val password: String
) {
    fun hasBlankFields(): Boolean {
        return listOf(
            identity, password
        ).any { it.isBlank() }
    }

    fun getValidationErrors(): String? = when {
        hasBlankFields() -> {
            "Inputs cannot be empty"
        }

        !AuthValidator.isValidEAddress(identity) -> {
            "Invalid email address"
        }

        else -> { // The data is valid
            null
        }
    }
}