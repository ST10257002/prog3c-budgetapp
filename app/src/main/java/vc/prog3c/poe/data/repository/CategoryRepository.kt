package vc.prog3c.poe.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import vc.prog3c.poe.data.models.Category
/**
 * @reference Firebase Firestore - Get Multiple Documents: https://firebase.google.com/docs/firestore/query-data/get-data#get_multiple_documents_from_a_collection
 * @reference Firebase Firestore - Add a Document with Auto-ID: https://firebase.google.com/docs/firestore/manage-data/add-data
 */

class CategoryRepository {
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    fun addCategory(category: Category, onComplete: (Boolean) -> Unit) {
        if (userId == null) {
            onComplete(false)
            return
        }

        db.collection("users")
            .document(userId)
            .collection("categories")
            .add(category)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getAllCategories(onComplete: (List<Category>?) -> Unit) {
        if (userId == null) {
            onComplete(null)
            return
        }

        db.collection("users")
            .document(userId)
            .collection("categories")
            .get()
            .addOnSuccessListener { result ->
                val categories = result.mapNotNull { it.toObject(Category::class.java) }
                onComplete(categories)
            }
            .addOnFailureListener { onComplete(null) }
    }
}