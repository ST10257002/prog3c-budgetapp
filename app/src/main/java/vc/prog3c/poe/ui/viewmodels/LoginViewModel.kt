package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.regex.Pattern
import vc.prog3c.poe.R
import java.util.Date

class LoginViewModel : ViewModel() {
    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _isProfileComplete = MutableLiveData<Boolean>()
    val isProfileComplete: LiveData<Boolean> = _isProfileComplete

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun login(identifier: String, password: String): Boolean {
        if (!isValidIdentifier(identifier)) {
            _error.value = "Please enter a valid email or username"
            return false
        }

        if (!isValidPassword(password)) {
            _error.value = getString(R.string.invalid_password)
            return false
        }

        // TODO: Implement Firestore Authentication
        // 1. Use Firebase Authentication to sign in user
        // 2. Check if user exists in Firestore 'users' collection
        // 3. If user exists, fetch user data and check if profile is complete
        // 4. If profile is incomplete, set _isProfileComplete to false
        // 5. Handle authentication errors and set appropriate error messages
        // 6. Store user session using Firebase Auth persistence
        // 7. Implement proper error handling for network issues
        _isLoggedIn.value = true
        _isProfileComplete.value = false
        return true
    }

    fun register(name: String, email: String, password: String, confirmPassword: String): Boolean {
        if (name.isBlank()) {
            _error.value = "Please enter your name"
            return false
        }

        if (!isValidEmail(email)) {
            _error.value = getString(R.string.invalid_email)
            return false
        }

        if (!isValidPassword(password)) {
            _error.value = getString(R.string.invalid_password)
            return false
        }

        if (password != confirmPassword) {
            _error.value = getString(R.string.passwords_dont_match)
            return false
        }

        // TODO: Implement Firestore Registration
        // 1. Use Firebase Authentication to create new user account
        // 2. Create new document in 'users' collection with user data:
        //    - userId (from Firebase Auth)
        //    - name
        //    - email
        //    - createdAt timestamp
        //    - isProfileComplete (false)
        // 3. Create subcollections for:
        //    - transactions
        //    - categories
        //    - savings_goals
        // 4. Set up security rules for user data
        // 5. Handle email verification
        // 6. Implement proper error handling for:
        //    - Email already in use
        //    - Network issues
        //    - Invalid email format
        _isLoggedIn.value = true
        _isProfileComplete.value = false
        return true
    }

    fun completeProfile(
        address: String,
        phoneNumber: String,
        cardNumber: String,
        cardType: String,
        cvc: String,
        expiryDate: String
    ): Boolean {
        if (address.isBlank() || phoneNumber.isBlank() || cardNumber.isBlank() || 
            cardType.isBlank() || cvc.isBlank() || expiryDate.isBlank()) {
            _error.value = "Please fill in all profile fields"
            return false
        }

        // TODO: Implement Firestore Profile Completion
        // 1. Update user document in 'users' collection with:
        //    - address
        //    - phoneNumber
        //    - paymentInfo (encrypted)
        //    - isProfileComplete (true)
        // 2. Implement proper encryption for sensitive data
        // 3. Add validation for:
        //    - Phone number format
        //    - Card number format
        //    - Expiry date format
        // 4. Set up security rules for payment info
        // 5. Handle network errors and timeouts
        _isProfileComplete.value = true
        return true
    }

    fun setGoals(minGoal: Double, maxGoal: Double, monthlyBudget: Double): Boolean {
        if (minGoal <= 0 || maxGoal <= 0 || monthlyBudget <= 0) {
            _error.value = "Please enter valid amounts"
            return false
        }

        if (minGoal > maxGoal) {
            _error.value = "Minimum goal cannot be greater than maximum goal"
            return false
        }

        // TODO: Implement Firestore Goals
        // 1. Create/update document in 'savings_goals' subcollection with:
        //    - minGoal
        //    - maxGoal
        //    - monthlyBudget
        //    - createdAt timestamp
        //    - updatedAt timestamp
        // 2. Set up triggers for:
        //    - Goal achievement notifications
        //    - Budget alerts
        // 3. Implement data validation
        // 4. Handle concurrent updates
        return true
    }

    fun bypassLogin() {
        // TODO: Implement Firestore Guest Mode
        // 1. Create a temporary guest user document in Firestore with:
        //    - userId: "guest_${timestamp}"
        //    - name: "Guest User"
        //    - email: "guest@example.com"
        //    - isGuest: true
        //    - createdAt: timestamp
        //    - isProfileComplete: true
        // 2. Set up guest data subcollections:
        //    - transactions: Predefined demo transactions
        //    - categories: Default categories
        //    - savings_goals: Sample goals
        //    - budget: Demo budget data
        // 3. Implement real-time listeners for guest data
        // 4. Set up data cleanup on guest logout
        // 5. Handle offline persistence
        // 6. Implement guest data expiration

        // For now, just set the login state
        _isLoggedIn.value = true
        _isProfileComplete.value = true
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
            Pattern.CASE_INSENSITIVE
        )
        return emailPattern.matcher(email).matches()
    }

    private fun isValidIdentifier(identifier: String): Boolean {
        return isValidEmail(identifier) || identifier.length >= 3
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    private fun getString(resourceId: Int): String {
        // TODO: Implement proper string resource handling
        return when (resourceId) {
            R.string.invalid_email -> "Please enter a valid email address"
            R.string.invalid_password -> "Password must be at least 6 characters"
            R.string.passwords_dont_match -> "Passwords do not match"
            else -> "Invalid input"
        }
    }
} 