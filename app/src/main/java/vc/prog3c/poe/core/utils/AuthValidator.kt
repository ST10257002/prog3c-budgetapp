package vc.prog3c.poe.core.utils

import android.util.Patterns

object AuthValidator {

    fun isValidEAddress(input: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(input).matches()
    }


    fun isMatchingCreds(input: String, match: String): Boolean = (input == match)


    fun isValidPassword(
        input: String
    ): Boolean {
        val limitForMinLength = 6
        val limitForMaxLength = 4096

        // Compare values against validation rules

        val inputWithinLength = input.length in limitForMinLength..limitForMaxLength
        val inputHasUpperCase = input.any { it.isUpperCase() }
        val inputHasLowerCase = input.any { it.isLowerCase() }
        val inputHasNumerical = input.any { it.isDigit() }
        val inputHasAnySymbol = input.any { !it.isLetterOrDigit() }
        val inputHasNoSpacing = input.any { !it.isWhitespace() }

        return !arrayOf(
            inputWithinLength,
            inputHasUpperCase,
            inputHasLowerCase,
            inputHasNumerical,
            inputHasAnySymbol,
            inputHasNoSpacing
        ).any {
            !it
        }
    }
}