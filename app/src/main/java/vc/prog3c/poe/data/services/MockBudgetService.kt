//package vc.prog3c.poe.data.services
//
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.map
//import vc.prog3c.poe.data.models.*
//import java.util.*
//
///**
// * Mock implementation of BudgetService for frontend development.
// * This will be replaced by the backend developer's Firestore implementation.
// */
//class MockBudgetService : BudgetService {
//    private val users = mutableMapOf<String, User>()
//    private val transactions = mutableMapOf<String, MutableList<Transaction>>()
//    private val goals = mutableMapOf<String, MutableList<SavingsGoal>>()
//
//    // State flows for real-time updates
//    private val userFlow = MutableStateFlow<User?>(null)
//    private val transactionsFlow = MutableStateFlow<List<Transaction>>(emptyList())
//    private val goalsFlow = MutableStateFlow<List<SavingsGoal>>(emptyList())
//
//    // User Operations
//    override suspend fun createUser(user: User): Result<User> = try {
//        users[user.id] = user
//        userFlow.value = user
//        Result.success<User>(user)
//    } catch (e: Exception) {
//        Result.failure(e)
//    }
//
//    override suspend fun getUser(userId: String): Result<User> = try {
//        val user = users[userId] ?: throw Exception("User not found")
//        Result.success<User>(user)
//    } catch (e: Exception) {
//        Result.failure(e)
//    }
//
//    override suspend fun updateUser(user: User): Result<User> = try {
//        users[user.id] = user
//        userFlow.value = user
//        Result.success<User>(user)
//    } catch (e: Exception) {
//        Result.failure(e)
//    }
//
//    override suspend fun updateProfileImage(userId: String, imageUri: String): Result<String> = try {
//        val user = users[userId] ?: throw Exception("User not found")
//        val updatedUser = user.copy(profilePictureUrl = imageUri)
//        users[userId] = updatedUser
//        userFlow.value = updatedUser
//        Result.success<String>(imageUri)
//    } catch (e: Exception) {
//        Result.failure(e)
//    }
//
//
//    // Transaction Operations
//    override suspend fun addTransaction(userId: String, transaction: Transaction): Result<Transaction> = try {
//        val userTransactions = transactions.getOrPut(userId) { mutableListOf() }
//        userTransactions.add(transaction)
//        transactionsFlow.value = userTransactions
//        Result.success<Transaction>(transaction)
//    } catch (e: Exception) {
//        Result.failure(e)
//    }
//
//    override suspend fun getTransactions(userId: String): Result<List<Transaction>> = try {
//        val userTransactions = transactions[userId] ?: emptyList()
//        Result.success<List<Transaction>>(userTransactions)
//    } catch (e: Exception) {
//        Result.failure(e)
//    }
//
//    override suspend fun getTransactionsByType(userId: String, type: TransactionType): Result<List<Transaction>> = try {
//        val userTransactions = transactions[userId] ?: emptyList()
//        val filteredTransactions = userTransactions.filter { it.type == type }
//        Result.success<List<Transaction>>(filteredTransactions)
//    } catch (e: Exception) {
//        Result.failure(e)
//    }
//
//    override suspend fun getTransactionsByDateRange(userId: String, startDate: Long, endDate: Long): Result<List<Transaction>> = try {
//        val userTransactions = transactions[userId] ?: emptyList()
//        val filteredTransactions = userTransactions.filter { it.date == startDate..endDate }
//        Result.success<List<Transaction>>(filteredTransactions)
//    } catch (e: Exception) {
//        Result.failure(e)
//    }
//
//    // Goal Operations
//    override suspend fun addGoal(userId: String, goal: SavingsGoal): Result<SavingsGoal> = try {
//        val userGoals = goals.getOrPut(userId) { mutableListOf() }
//        userGoals.add(goal)
//        goalsFlow.value = userGoals
//        Result.success<SavingsGoal>(goal)
//    } catch (e: Exception) {
//        Result.failure(e)
//    }
//
//    override suspend fun getGoals(userId: String): Result<List<SavingsGoal>> = try {
//        val userGoals = goals[userId] ?: emptyList()
//        Result.success<List<SavingsGoal>>(userGoals)
//    } catch (e: Exception) {
//        Result.failure(e)
//    }
//
//    override suspend fun updateGoal(userId: String, goalId: String, goal: SavingsGoal): Result<SavingsGoal> = try {
//        val userGoals = goals[userId] ?: throw Exception("No goals found")
//        val index = userGoals.indexOfFirst { it.userId == goalId }
//        if (index != -1) {
//            userGoals[index] = goal
//            goalsFlow.value = userGoals
//            Result.success<SavingsGoal>(goal)
//        } else {
//            Result.failure<SavingsGoal>(Exception("Goal not found"))
//        }
//    } catch (e: Exception) {
//        Result.failure<SavingsGoal>(e)
//    }
//
//    override suspend fun deleteGoal(userId: String, goalId: String): Result<Unit> = try {
//        val userGoals = goals[userId] ?: throw Exception("No goals found")
//        userGoals.removeIf { it.userId == goalId }
//        goalsFlow.value = userGoals
//        Result.success<Unit>(Unit)
//    } catch (e: Exception) {
//        Result.failure(e)
//    }
//
//    // Real-time Updates
//    override fun observeUser(userId: String): Flow<User> = userFlow.map { it ?: throw Exception("User not found") }
//    override fun observeTransactions(userId: String): Flow<List<Transaction>> = transactionsFlow
//    override fun observeGoals(userId: String): Flow<List<SavingsGoal>> = goalsFlow
//
//    companion object {
//        @Volatile
//        private var INSTANCE: MockBudgetService? = null
//
//        fun getInstance(): MockBudgetService {
//            return INSTANCE ?: synchronized(this) {
//                INSTANCE ?: MockBudgetService().also { INSTANCE = it }
//            }
//        }
//    }
//
//    // Test user data
//    init {
//        val imageUri = ""
//        users["user1"] = User(
//            id = "user1",
//            name = "John Doe",
//            email = "john.doe@example.com",
//            totalBalance = 50000.0,
//            profilePictureUrl = imageUri
//        )
//    }
//}