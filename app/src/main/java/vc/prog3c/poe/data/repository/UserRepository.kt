package vc.prog3c.poe.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import vc.prog3c.poe.data.models.User
/**
 * @reference Firebase Auth - Current User: https://firebase.google.com/docs/auth/android/manage-users#get_the_currently_signed-in_user
 * @reference Firebase Firestore - Set a Document: https://firebase.google.com/docs/firestore/manage-data/add-data#set_a_document
 */

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun addUser(user: User, onComplete: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("FIRESTORE", "No user signed in")
            onComplete(false)
            return
        }

        db.collection("users")
            .document(userId)
            .set(user)
            .addOnSuccessListener {
                Log.d("FIRESTORE_WRITE", "User added successfully with ID: $userId")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e("FIRESTORE_WRITE", "Failed to add user: ${e.message}")
                onComplete(false)
            }
    }

    fun getUser(onComplete: (User?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("FIRESTORE", "No user signed in")
            onComplete(null)
            return
        }

        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                Log.d("FIRESTORE_READ", "Fetched user: $user")
                onComplete(user)
            }
            .addOnFailureListener { e ->
                Log.e("FIRESTORE_READ", "Failed to fetch user: ${e.message}")
                onComplete(null)
            }
    }

    fun updateUser(user: User, onComplete: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onComplete(false)

        db.collection("users")
            .document(userId)
            .set(user)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

}