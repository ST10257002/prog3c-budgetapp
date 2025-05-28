package vc.prog3c.poe.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import vc.prog3c.poe.data.models.User

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val userId: String
        get() = auth.currentUser?.uid ?: ""

    /**
     * Adds or overwrites a user profile in Firestore.
     */
    suspend fun addUser(user: User): Result<Unit> = runCatching {
        db.collection("users")
            .document(userId)
            .set(user)
            .await()
    }

    /**
     * Fetches the user profile from Firestore.
     */
    suspend fun getUser(): Result<User?> = runCatching {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: throw IllegalStateException("No user signed in")
        val doc = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .await()

        doc.toObject(User::class.java)
    }

    /**
     * Updates user profile in Firestore.
     */
    suspend fun updateUser(user: User): Result<Unit> = runCatching {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: throw IllegalStateException("No user signed in")
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .set(user)
            .await()
    }
}
