package vc.prog3c.poe.data.services

import vc.prog3c.poe.data.models.*
import kotlinx.coroutines.flow.Flow

/**
 * Service interface defining the contract between frontend and backend.
 * The backend developer will implement these methods using Firestore.
 */
interface BudgetService {
    // User Operations
    suspend fun createUser(user: User): Result<User>
    suspend fun getUser(userId: String): Result<User>
    suspend fun updateUser(user: User): Result<User>
    suspend fun updateProfileImage(userId: String, imageUri: String): Result<String>

    // Card Operations
    suspend fun addCard(userId: String, card: CardDetails): Result<CardDetails>
    suspend fun getCards(userId: String): Result<List<CardDetails>>
    suspend fun deleteCard(userId: String, cardId: String): Result<Unit>

    // Transaction Operations
    suspend fun addTransaction(userId: String, transaction: Transaction): Result<Transaction>
    suspend fun getTransactions(userId: String): Result<List<Transaction>>
    suspend fun getTransactionsByType(userId: String, type: TransactionType): Result<List<Transaction>>
    suspend fun getTransactionsByDateRange(userId: String, startDate: Long, endDate: Long): Result<List<Transaction>>

    // Goal Operations
    suspend fun addGoal(userId: String, goal: SavingsGoal): Result<SavingsGoal>
    suspend fun getGoals(userId: String): Result<List<SavingsGoal>>
    suspend fun updateGoal(userId: String, goalId: String, goal: SavingsGoal): Result<SavingsGoal>
    suspend fun deleteGoal(userId: String, goalId: String): Result<Unit>

    // Real-time Updates
    fun observeUser(userId: String): Flow<User>
    fun observeTransactions(userId: String): Flow<List<Transaction>>
    fun observeGoals(userId: String): Flow<List<SavingsGoal>>
    fun observeCards(userId: String): Flow<List<CardDetails>>
} 