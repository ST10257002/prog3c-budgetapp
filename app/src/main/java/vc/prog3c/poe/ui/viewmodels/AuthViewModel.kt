package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import vc.prog3c.poe.data.models.User
import vc.prog3c.poe.data.services.FirestoreService

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadUserProfile() {
        FirestoreService.user.getUser { user ->
            _currentUser.postValue(user)
        }
    }

    fun completeUserProfile(update: User) {
        FirestoreService.user.updateUser(update) { success ->
            if (success) {
                _currentUser.postValue(update)
            } else {
                _error.postValue("Failed to update user profile")
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _isLoggedIn.value = false
        _currentUser.value = null
    }
}
