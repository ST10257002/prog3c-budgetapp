package vc.prog3c.poe.data.services

import vc.prog3c.poe.data.repository.*

object FirestoreService {
    val savingsGoal = SavingsGoalRepository()
    val user = UserRepository()
    val category = CategoryRepository()
    val transaction = TransactionRepository()
    val account = AccountRepository()
    val budget = BudgetRepository()
}
