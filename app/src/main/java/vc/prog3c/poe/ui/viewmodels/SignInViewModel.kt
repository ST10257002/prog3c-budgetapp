package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import vc.prog3c.poe.core.services.AuthService
import vc.prog3c.poe.core.utils.AuthValidator
import vc.prog3c.poe.core.utils.Blogger
import vc.prog3c.poe.ui.views.SignInActivity

/**
 * Viewmodel for [SignInActivity]
 */
class SignInViewModel : ViewModel() {


    private var authService = AuthService()


    // --- Fields


    private val _uiState = MutableLiveData<SignInUiState>()
    val uiState: LiveData<SignInUiState> = _uiState


    // --- Functions


    fun signIn(
        identity: String, password: String
    ) {
        if (identity.isBlank() && password.isBlank()) {
            _uiState.value = SignInUiState.Failure("Login failed")
            return
        }

        // TODO: Add username compatibility

        if (!(AuthValidator.isValidEAddress(identity))) {
            _uiState.value = SignInUiState.Failure("Invalid email address")
            return
        }

        authenticate(
            identity, password
        )
    }


    fun getCurrentUser(): FirebaseUser? = authService.getCurrentUser()


    fun bypassLogin() {
        _uiState.value = SignInUiState.Success
    }


    // --- Internals


    private fun authenticate(
        email: String, password: String
    ) {
        authService.signIn(
            email, password, ::onSignInSuccess, ::onSignInFailure
        )
    }


    private fun onSignInSuccess() {
        Blogger.i(
            getTag(), "Successfully logged in"
        )

        _uiState.value = SignInUiState.Success
    }


    private fun onSignInFailure(message: String) {
        Blogger.d(
            getTag(), "Sign-in failed: $message"
        )

        _uiState.value = SignInUiState.Failure(message)
    }


    // --- Helpers


    private fun getTag() = this::class.java.simpleName.toString()
}