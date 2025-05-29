package vc.prog3c.poe.core.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

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
                        task.exception?.localizedMessage ?: "Something went wrong"
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
                        task.exception?.localizedMessage ?: "Something went wrong"
                    )
                }
            }
        }
    }


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