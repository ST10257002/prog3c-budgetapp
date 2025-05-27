package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Date
import vc.prog3c.poe.data.models.Goal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GoalViewModel : ViewModel() {
    // TODO: Replace with Firestore implementation
    // - Create Firestore collection for goals
    // - Implement real-time listeners for goal updates
    // - Add offline persistence support
    // - Implement data synchronization
    // - Add error handling for network issues

    private val _goals = MutableLiveData<List<Goal>>()
    val goals: LiveData<List<Goal>> = _goals

    private val _activeGoals = MutableLiveData<List<Goal>>()
    val activeGoals: LiveData<List<Goal>> = _activeGoals

    private val _completedGoals = MutableLiveData<List<Goal>>()
    val completedGoals: LiveData<List<Goal>> = _completedGoals

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadTestData()
    }

    private fun loadTestData() {
        val currentDate = Date()
        val oneMonthFromNow = Date(currentDate.time + 30L * 24 * 60 * 60 * 1000)
        val threeMonthsFromNow = Date(currentDate.time + 90L * 24 * 60 * 60 * 1000)

        // Test data for goals
        _goals.value = listOf(
            Goal(
                id = "1",
                name = "New Laptop",
                targetAmount = 15000.0,
                currentAmount = 5000.0,
                deadline = threeMonthsFromNow,
                category = "Electronics",
                description = "Save for a new MacBook Pro"
            ),
            Goal(
                id = "2",
                name = "Vacation Fund",
                targetAmount = 25000.0,
                currentAmount = 15000.0,
                deadline = oneMonthFromNow,
                category = "Travel",
                description = "Save for summer vacation"
            ),
            Goal(
                id = "3",
                name = "Emergency Fund",
                targetAmount = 50000.0,
                currentAmount = 50000.0,
                deadline = currentDate,
                category = "Savings",
                description = "Build emergency fund",
                isCompleted = true
            )
        )

        updateGoalLists()
    }

    private fun updateGoalLists() {
        _goals.value?.let { allGoals ->
            _activeGoals.value = allGoals.filter { !it.isCompleted }
            _completedGoals.value = allGoals.filter { it.isCompleted }
        }
    }

    fun addGoal(goal: Goal) {
        // TODO: Implement Firestore goal addition
        // - Add to Firestore collection
        // - Handle offline persistence
        // - Implement error handling
        val currentList = _goals.value?.toMutableList() ?: mutableListOf()
        currentList.add(goal)
        _goals.value = currentList
        updateGoalLists()
    }

    fun updateGoal(goal: Goal) {
        // TODO: Implement Firestore goal update
        // - Update Firestore document
        // - Handle offline persistence
        // - Implement error handling
        val currentList = _goals.value?.toMutableList() ?: return
        val index = currentList.indexOfFirst { it.id == goal.id }
        if (index != -1) {
            currentList[index] = goal
            _goals.value = currentList
            updateGoalLists()
        }
    }

    fun deleteGoal(goalId: String) {
        // TODO: Implement Firestore goal deletion
        // - Delete from Firestore collection
        // - Handle offline persistence
        // - Implement error handling
        val currentList = _goals.value?.toMutableList() ?: return
        currentList.removeIf { it.id == goalId }
        _goals.value = currentList
        updateGoalLists()
    }

    fun updateGoalProgress(goalId: String, newAmount: Double) {
        // TODO: Implement Firestore goal progress update
        // - Update Firestore document
        // - Handle offline persistence
        // - Implement error handling
        val currentList = _goals.value?.toMutableList() ?: return
        val index = currentList.indexOfFirst { it.id == goalId }
        if (index != -1) {
            val goal = currentList[index]
            val updatedGoal = goal.copy(
                currentAmount = newAmount,
                isCompleted = newAmount >= goal.targetAmount
            )
            currentList[index] = updatedGoal
            _goals.value = currentList
            updateGoalLists()
        }
    }

    fun validateGoal(minGoal: Double, maxGoal: Double, monthlyBudget: Double): Boolean {
        return when {
            minGoal <= 0 || maxGoal <= 0 || monthlyBudget <= 0 -> {
                _error.value = "Values must be greater than 0"
                false
            }
            minGoal > maxGoal -> {
                _error.value = "Min goal cannot be greater than max goal"
                false
            }
            else -> {
                _error.value = null
                true
            }
        }
    }

    fun saveValidatedGoalToFirestore(min: Double, max: Double, budget: Double, onResult: (Boolean) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return onResult(false)

        val goalData = mapOf(
            "minMonthlyGoal" to min,
            "maxMonthlyGoal" to max,
            "monthlyBudget" to budget,
            "createdAt" to System.currentTimeMillis()
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("goals")
            .document("primary_savings_goal")
            .set(goalData)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
} 