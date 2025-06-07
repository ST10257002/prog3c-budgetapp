package vc.prog3c.poe.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val formatter = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    fun format(amount: Double): String {
        return formatter.format(amount)
    }

    fun format(amount: Int): String {
        return formatter.format(amount)
    }

    fun format(amount: Long): String {
        return formatter.format(amount)
    }

    fun format(amount: Float): String {
        return formatter.format(amount)
    }
} 