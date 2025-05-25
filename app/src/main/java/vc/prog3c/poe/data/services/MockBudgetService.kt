package vc.prog3c.poe.data.services

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import vc.prog3c.poe.data.models.*
import java.util.*

/**
 * Mock implementation of BudgetService for frontend development.
 * This will be replaced by the backend developer's Firestore implementation.
 */
class MockBudgetService : BudgetService {
    private val users = mutableMapOf<String, User>()
    private val cards = mutableMapOf<String, MutableList<CardDetails>>()
    private val transactions = mutableMapOf<String, MutableList<Transaction>>()
    private val goals = mutableMapOf<String, MutableList<SavingsGoal>>()

    // State flows for real-time updates
    private val userFlow = MutableStateFlow<User?>(null)
    private val cardsFlow = MutableStateFlow<List<CardDetails>>(emptyList())
    private val transactionsFlow = MutableStateFlow<List<Transaction>>(emptyList())
    private val goalsFlow = MutableStateFlow<List<SavingsGoal>>(emptyList())

    // User Operations
    override suspend fun createUser(user: User): Result<User> = try {
        users[user.id] = user
        userFlow.value = user
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getUser(userId: String): Result<User> = try {
        val user = users[userId] ?: throw Exception("User not found")
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateUser(user: User): Result<User> = try {
        users[user.id] = user
        userFlow.value = user
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateProfileImage(userId: String, imageUri: String): Result<String> = try {
        val user = users[userId] ?: throw Exception("User not found")
        val updatedUser = user.copy(profilePictureUrl = imageUri)
        users[userId] = updatedUser
        userFlow.value = updatedUser
        Result.success(imageUri)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Card Operations
    override suspend fun addCard(userId: String, card: CardDetails): Result<CardDetails> = try {
        val userCards = cards.getOrPut(userId) { mutableListOf() }
        userCards.add(card)
        cardsFlow.value = userCards
        Result.success(card)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getCards(userId: String): Result<List<CardDetails>> = try {
        val userCards = cards[userId] ?: emptyList()
        Result.success(userCards)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteCard(userId: String, cardId: String): Result<Unit> = try {
        val userCards = cards[userId] ?: throw Exception("No cards found")
        userCards.removeIf { it.cardNumber == cardId }
        cardsFlow.value = userCards
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Transaction Operations
    override suspend fun addTransaction(userId: String, transaction: Transaction): Result<Transaction> = try {
        val userTransactions = transactions.getOrPut(userId) { mutableListOf() }
        userTransactions.add(transaction)
        transactionsFlow.value = userTransactions
        Result.success(transaction)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getTransactions(userId: String): Result<List<Transaction>> = try {
        val userTransactions = transactions[userId] ?: emptyList()
        Result.success(userTransactions)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getTransactionsByType(userId: String, type: TransactionType): Result<List<Transaction>> = try {
        val userTransactions = transactions[userId] ?: emptyList()
        val filteredTransactions = userTransactions.filter { it.type == type }
        Result.success(filteredTransactions)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getTransactionsByDateRange(userId: String, startDate: Long, endDate: Long): Result<List<Transaction>> = try {
        val userTransactions = transactions[userId] ?: emptyList()
        val filteredTransactions = userTransactions.filter { it.date in startDate..endDate }
        Result.success(filteredTransactions)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Goal Operations
    override suspend fun addGoal(userId: String, goal: SavingsGoal): Result<SavingsGoal> = try {
        val userGoals = goals.getOrPut(userId) { mutableListOf() }
        userGoals.add(goal)
        goalsFlow.value = userGoals
        Result.success(goal)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getGoals(userId: String): Result<List<SavingsGoal>> = try {
        val userGoals = goals[userId] ?: emptyList()
        Result.success(userGoals)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateGoal(userId: String, goalId: String, goal: SavingsGoal): Result<SavingsGoal> = try {
        val userGoals = goals[userId] ?: throw Exception("No goals found")
        val index = userGoals.indexOfFirst { it.userId == goalId }
        if (index != -1) {
            userGoals[index] = goal
            goalsFlow.value = userGoals
            Result.success(goal)
        } else {
            Result.failure(Exception("Goal not found"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteGoal(userId: String, goalId: String): Result<Unit> = try {
        val userGoals = goals[userId] ?: throw Exception("No goals found")
        userGoals.removeIf { it.userId == goalId }
        goalsFlow.value = userGoals
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Real-time Updates
    override fun observeUser(userId: String): Flow<User> = userFlow.map { it ?: throw Exception("User not found") }
    override fun observeTransactions(userId: String): Flow<List<Transaction>> = transactionsFlow
    override fun observeGoals(userId: String): Flow<List<SavingsGoal>> = goalsFlow
    override fun observeCards(userId: String): Flow<List<CardDetails>> = cardsFlow

    companion object {
        @Volatile
        private var INSTANCE: MockBudgetService? = null

        fun getInstance(): MockBudgetService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MockBudgetService().also { INSTANCE = it }
            }
        }
    }
}