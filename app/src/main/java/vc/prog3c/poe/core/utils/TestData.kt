package vc.prog3c.poe.utils

import android.util.Log
import vc.prog3c.poe.data.models.User
import vc.prog3c.poe.data.services.FirestoreService

object TestData {

    fun runFirestoreTest() {
        val fakeUser = User(
            id = "testUser1234",
            email = "testdata@gmail.com",
            name = "Test Dev User",

            profilePictureUrl = "",

        )

        // Write test
        FirestoreService.users.addUser(fakeUser) { success ->
            Log.d("FIRESTORE_WRITE", "Write success? $success")
        }

        // Read test
        FirestoreService.users.getUser { user ->
            Log.d("FIRESTORE_READ", "Fetched user: $user")
        }
    }
}
