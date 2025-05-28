package vc.prog3c.poe.data.services

import vc.prog3c.poe.data.repository.*

object FirestoreService {
    val savingsGoal = SavingsGoalRepository()
    val users = UserRepository()
    val transaction = TransactionRepository() // expenses and income
    val account = AccountRepository()
    val budget = BudgetRepository()
}


//Example on how we could use this
// FirestoreService.expenses.getAllExpenses { ... }
