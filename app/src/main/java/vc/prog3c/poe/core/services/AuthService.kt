package vc.prog3c.poe.core.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthService(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    suspend fun signUpAsync(
        usermail: String, password: String,
    ): FirebaseUser {
        val result = auth.createUserWithEmailAndPassword(usermail, password).await()
        when (result.user) {
            null -> throw IllegalStateException("User is null after sign-up")
            else -> return result.user!!
        }
    }
    
    suspend fun signInAsync(
        usermail: String, password: String
    ): FirebaseUser {
        val result = auth.signInWithEmailAndPassword(usermail, password).await()
        when (result.user) {
            null -> throw IllegalStateException("User is null after sign-in")
            else -> return result.user!!
        }
    }
    
    // --- Extensions
    
    suspend fun sendPasswordResetEmailAsync(email: String) {
        try {
            auth.sendPasswordResetEmail(email).await()
        } catch (e: Exception) {
            throw IllegalStateException(
                "Failed to send password reset email", e
            )
        }
    }
    
    suspend fun deleteCurrentUserAsync() {
        val user = auth.currentUser
        when (user) {
            null -> throw IllegalStateException("Could not find authenticated user")
            else -> user.delete().await()
        }
    }
    
    fun isUserSignedIn(): Boolean = (auth.currentUser != null)
    
    fun getCurrentUser(): FirebaseUser? = auth.currentUser
    
    fun logout() {
        auth.signOut()
    }
}