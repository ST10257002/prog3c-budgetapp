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

/*
    fun bypassLogin() {
        _uiState.value = SignInUiState.Success
    }
*/

    // testing random user creation
    fun bypassLogin() {
        // Create a random user and sign in with it
        val randomSuffix = (10000..99999).random()
        val randomEmail = "test$randomSuffix@example.com"
        val randomPassword = "TestPass123!"

        authService.signUp(
            randomEmail, randomPassword,
            {
                authService.signIn(
                    randomEmail, randomPassword,
                    {
                        Blogger.i(getTag(), "Bypassed login with random user: $randomEmail")
                        _uiState.value = SignInUiState.Success
                    },
                    { message ->
                        Blogger.e(getTag(), "Sign-in failed after signup: $message")
                        _uiState.value = SignInUiState.Failure(message)
                    }
                )
            },
            { message ->
                Blogger.e(getTag(), "Signup failed: $message")
                _uiState.value = SignInUiState.Failure(message)
            }
        )
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