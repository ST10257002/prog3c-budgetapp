package vc.prog3c.poe.data.services

import vc.prog3c.poe.data.repository.*

object FirestoreService {
    val incomes = IncomeRepository()
    val expenses = ExpenseRepository()
    val savingsGoal = SavingsGoalRepository()
    val users = UserRepository()
    val categories = CategoryRepository()
    val transaction = TransactionRepository()
}


//Example on how we could use this
// FirestoreService.expenses.getAllExpenses { ... }
