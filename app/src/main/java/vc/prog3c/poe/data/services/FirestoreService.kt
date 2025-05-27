package vc.prog3c.poe.data.services

import vc.prog3c.poe.data.repository.*

object FirestoreService {
    val expenses = ExpenseRepository()
    val incomes = IncomeRepository()
    val categories = CategoryRepository()
    val users = UserRepository()
    val savingsGoal = SavingsGoalRepository
}

//Example on how we could use this
// FirestoreService.expenses.getAllExpenses { ... }
