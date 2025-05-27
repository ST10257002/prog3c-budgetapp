package vc.prog3c.poe.core.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthService(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    fun signUp(
        usermail: String,
        password: String,
        onSuccessCallback: () -> Unit,
        onFailureCallback: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(usermail, password).apply {
            addOnCompleteListener { task ->
                when (task.isSuccessful) {
                    true -> onSuccessCallback()
                    else -> onFailureCallback(
                        task.exception?.localizedMessage ?: "Registration failed"
                    )
                }
            }
        }
    }

    
    fun signIn(
        usermail: String,
        password: String,
        onSuccessCallback: () -> Unit,
        onFailureCallback: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(usermail, password).apply {
            addOnCompleteListener { task ->
                when (task.isSuccessful) {
                    true -> onSuccessCallback()
                    else -> onFailureCallback(
                        task.exception?.localizedMessage ?: "Login failed"
                    )
                }
            }
        }
    }
    
    
    fun isUserSignedIn() : Boolean = (auth.currentUser != null)
    
    
    fun getCurrentUser() : FirebaseUser? = auth.currentUser
    
    
    fun logout() {
        auth.signOut()
    }
}