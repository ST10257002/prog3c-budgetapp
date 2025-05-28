package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import vc.prog3c.poe.data.models.User
import vc.prog3c.poe.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _isLoggedIn = MutableLiveData<Boolean>().apply {
        value = auth.currentUser != null
    }
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /**
     * Load current user profile from Firestore using coroutine
     */
    fun loadUserProfile() = viewModelScope.launch {
        val result = userRepository.getUser()
        result
            .onSuccess { _currentUser.postValue(it) }
            .onFailure { _error.postValue("Failed to fetch user: ${it.localizedMessage}") }
    }

    /**
     * Update Firestore user document with new data
     */
    fun completeUserProfile(updatedUser: User) = viewModelScope.launch {
        val result = userRepository.updateUser(updatedUser)
        result
            .onSuccess { _currentUser.postValue(updatedUser) }
            .onFailure { _error.postValue("Failed to update user profile: ${it.localizedMessage}") }
    }

    /**
     * Firebase logout + state clear
     */
    fun signOut() {
        auth.signOut()
        _isLoggedIn.value = false
        _currentUser.value = null
    }
}
