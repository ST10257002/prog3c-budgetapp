package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vc.prog3c.poe.core.services.AuthService
import vc.prog3c.poe.core.utils.AuthValidator
import vc.prog3c.poe.core.utils.Blogger

class SignUpViewModel : ViewModel() {

    
    private var authService = AuthService()


    // --- Fields


    private val _uiState = MutableLiveData<SignUpUiState>()
    val uiState: LiveData<SignUpUiState> = _uiState


    // --- Functions


    fun signUp(
        username: String,
        defaultPassword: String,
        confirmPassword: String,
        usermail: String,
        name: String,
        surname: String
    ) {
        val credentialArray = arrayOf(
            username, defaultPassword, confirmPassword, usermail, name, surname
        )
        
        if (credentialArray.any { it.isBlank() }) {
            _uiState.value = SignUpUiState.Failure(
                "Inputs cannot be empty"
            )
            return
        }

        if (!(AuthValidator.isValidPassword(defaultPassword))) {
            _uiState.value = SignUpUiState.Failure(
                "Passwords don't meet the complexity rules"
            )
            return
        }

        if (defaultPassword != confirmPassword) {
            _uiState.value = SignUpUiState.Failure(
                "The provided passwords don't match"
            )
            return
        }

        if (!(AuthValidator.isValidEAddress(usermail))) {
            _uiState.value = SignUpUiState.Failure(
                "Invalid email address"
            )
            return
        }

        authenticate(
            usermail, defaultPassword
        )
    }


    // --- Internals


    private fun authenticate(
        email: String, password: String
    ) {
        authService.signUp(
            email, password, ::onSignUpSuccess, ::onSignUpFailure
        )
    }


    private fun onSignUpSuccess() {
        Blogger.i(
            getTag(), "Successfully registered new account"
        )

        _uiState.value = SignUpUiState.Success
    }


    private fun onSignUpFailure(message: String) {
        Blogger.d(
            getTag(), "Sign-up failed: $message"
        )

        _uiState.value = SignUpUiState.Failure(message)
    }


    // --- Helpers


    private fun getTag() = this::class.java.simpleName.toString()
}