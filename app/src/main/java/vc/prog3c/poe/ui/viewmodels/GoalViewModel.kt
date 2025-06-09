package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import vc.prog3c.poe.core.services.AuthService
import vc.prog3c.poe.data.models.SavingsGoal
import vc.prog3c.poe.data.services.FirestoreService
import java.util.Date
import java.util.UUID
/**
 * @reference Firebase Firestore - Save and Retrieve Documents: https://firebase.google.com/docs/firestore/manage-data/add-data
 * @reference Firebase Firestore - Timestamp & Date Usage: https://firebase.google.com/docs/reference/kotlin/com/google/firebase/Timestamp
 */

// TODO: Replace with Firestore implementation
// - Create Firestore collection for goals
// - Implement real-time listeners for goal updates
// - Add offline persistence support
// - Implement data synchronization
// - Add error handling for network issues
class GoalViewModel(
    private val authService: AuthService = AuthService(),
    private val dataService: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {
    companion object {
        private const val TAG = "GoalViewModel"
    }
    
    // --- Fields
    private val _goals = MutableLiveData<List<SavingsGoal>>()
    val goals: LiveData<List<SavingsGoal>> = _goals

    private val _activeGoals = MutableLiveData<List<SavingsGoal>>()
    val activeGoals: LiveData<List<SavingsGoal>> = _activeGoals

    private val _completedGoals = MutableLiveData<List<SavingsGoal>>()
    val completedGoals: LiveData<List<SavingsGoal>> = _completedGoals

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadGoals()
    }
    
    // --- Internals
    private fun loadGoals() {
        _isLoading.value = true
        FirestoreService.savingsGoal.fetchGoals { goals ->
            _isLoading.value = false
            _goals.value = goals
            updateGoalLists()
        }
    }

    private fun updateGoalLists() {
        _goals.value?.let { allGoals ->
            _activeGoals.value = allGoals.filter { it.savedAmount < it.targetAmount }
            _completedGoals.value = allGoals.filter { it.savedAmount >= it.targetAmount }
        }
    }

    fun loadCurrentGoal(callback: (SavingsGoal?) -> Unit) {
        _isLoading.value = true
        FirestoreService.savingsGoal.getCurrentGoal { goal ->
            _isLoading.value = false
            callback(goal)
        }
    }

    fun validateGoal(minGoal: Double, maxGoal: Double, monthlyBudget: Double): Boolean {
        if (minGoal <= 0 || maxGoal <= 0 || monthlyBudget <= 0) {
            _error.value = "All amounts must be greater than 0"
            return false
        }

        if (minGoal > maxGoal) {
            _error.value = "Minimum goal cannot be greater than maximum goal"
            return false
        }

        if (monthlyBudget < minGoal) {
            _error.value = "Monthly budget must be at least equal to minimum goal"
            return false
        }

        return true
    }

    fun saveValidatedGoalToFirestore(goalName: String, minGoal: Double, maxGoal: Double, monthlyBudget: Double, callback: (Boolean) -> Unit) {
        _isLoading.value = true

        // First get the current goal to update it
        loadCurrentGoal { currentGoal ->
            if (currentGoal != null) {
                // Update existing goal
                val updates = mapOf<String, Any>(
                    "name" to goalName,
                    "targetAmount" to maxGoal,
                    "minMonthlyGoal" to minGoal,
                    "maxMonthlyGoal" to maxGoal,
                    "monthlyBudget" to monthlyBudget
                )

                FirestoreService.savingsGoal.updateGoal(currentGoal.id, updates) { success ->
                    _isLoading.value = false
                    if (success) {
                        // Update local state
                        _goals.value = _goals.value?.map { 
                            if (it.id == currentGoal.id) {
                                it.copy(
                                    name = goalName,
                                    targetAmount = maxGoal,
                                    minMonthlyGoal = minGoal,
                                    maxMonthlyGoal = maxGoal,
                                    monthlyBudget = monthlyBudget
                                )
                            } else it 
                        }
                        updateGoalLists()
                    } else {
                        _error.value = "Failed to update goal"
                    }
                    callback(success)
                }
            } else {
                // Create new goal if none exists
                val goal = SavingsGoal(
                    id = UUID.randomUUID().toString(),
                    name = goalName,
                    targetAmount = maxGoal,
                    savedAmount = 0.0,
                    targetDate = Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000), // 30 days from now
                    minMonthlyGoal = minGoal,
                    maxMonthlyGoal = maxGoal,
                    monthlyBudget = monthlyBudget
                )

                FirestoreService.savingsGoal.saveGoal(goal) { success ->
                    _isLoading.value = false
                    if (success) {
                        _goals.value = (_goals.value ?: emptyList()) + goal
                        updateGoalLists()
                    } else {
                        _error.value = "Failed to save goal"
                    }
                    callback(success)
                }
            }
        }
    }
} 