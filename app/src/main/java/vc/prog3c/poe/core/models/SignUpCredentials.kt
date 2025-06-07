package vc.prog3c.poe.core.models

import vc.prog3c.poe.core.utils.AuthValidator

/**
 * Data transfer object for registering a user.
 */
data class SignUpCredentials(
    val defaultPassword: String,
    val confirmPassword: String,
    val usermail: String,
    val name: String,
    val surname: String
) {
    fun hasBlankFields(): Boolean {
        return listOf( // Check if any fields are empty or contains only spaces
            defaultPassword, confirmPassword, usermail, name, surname
        ).any { it.isBlank() }
    }
    

    fun getValidationErrors(): String? = when {
        hasBlankFields() -> {
            "Inputs cannot be empty"
        }
        
        !AuthValidator.isValidPassword(defaultPassword) -> {
            "Passwords don't meet the complexity rules"
        }

        !AuthValidator.isMatchingCreds(defaultPassword, confirmPassword) -> {
            "The provided passwords don't match"
        }

        !AuthValidator.isValidEAddress(usermail) -> {
            "Invalid email address"
        }

        else -> { // The data is valid
            null
        }
    }
}