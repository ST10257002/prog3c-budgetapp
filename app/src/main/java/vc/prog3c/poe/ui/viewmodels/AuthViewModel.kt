package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import vc.prog3c.poe.R
import java.util.regex.Pattern

class AuthViewModel : ViewModel() {
    
    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn


    private val _isProfileComplete = MutableLiveData<Boolean>()
    val isProfileComplete: LiveData<Boolean> = _isProfileComplete


    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    
    
    fun authenticate(
        usermail: String, password: String
    ) {
        if (usermail.isBlank() && password.isBlank()) {
            _error.value = "Login failed"
            return
        }
        
        var auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(usermail, password).apply { 
            addOnCompleteListener { task -> 
                when (task.isSuccessful) {
                    true -> {
                        _isLoggedIn.value = true
                        _isProfileComplete.value = false
                    }
                    else -> {
                        _error.value = "Login failed"
                    }
                }
            }
        }
    }


    fun register(
        name: String, email: String, password: String, confirmPassword: String
    ): Boolean {
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
        if (arrayOf(address, phoneNumber, cardNumber, cardType, cvc, expiryDate).any {
                it.isBlank()
            }) {
            _error.value = "Please fill in all profile fields"
            return false
        }

        _isProfileComplete.value = true
        return true
    }


    fun setGoals(
        minGoal: Double, maxGoal: Double, monthlyBudget: Double
    ): Boolean {
        if (minGoal <= 0 || maxGoal <= 0 || monthlyBudget <= 0) {
            _error.value = "Please enter valid amounts"
            return false
        }

        if (minGoal > maxGoal) {
            _error.value = "Minimum goal cannot be greater than maximum goal"
            return false
        }

        return true
    }


    fun bypassLogin() {
        _isLoggedIn.value = true
        _isProfileComplete.value = true
    }


    private fun isValidEmail(
        email: String
    ): Boolean {
        val emailPattern = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
            Pattern.CASE_INSENSITIVE
        )

        return emailPattern.matcher(email).matches()
    }


    private fun isValidIdentifier(
        identifier: String
    ): Boolean {
        return (isValidEmail(identifier) || identifier.length >= 3)
    }


    private fun isValidPassword(
        password: String
    ): Boolean {
        return password.length >= 6
    }


    private fun getString(
        resourceId: Int
    ): String {
        return when (resourceId) {
            R.string.invalid_email -> "Please enter a valid email address"
            R.string.invalid_password -> "Password must be at least 6 characters"
            R.string.passwords_dont_match -> "Passwords do not match"
            else -> "Invalid input"
        }
    }
} 