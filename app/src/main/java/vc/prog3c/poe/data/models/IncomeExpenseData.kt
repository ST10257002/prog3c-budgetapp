package vc.prog3c.poe.data.models

import com.github.mikephil.charting.data.PieData

data class IncomeExpenseData(
    val totalIncome: Double,
    val totalExpenses: Double,
    val pieData: PieData
)
