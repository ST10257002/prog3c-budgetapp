package vc.prog3c.poe.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import vc.prog3c.poe.data.models.Category

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

    fun syncCategoryToBudget(category: Category) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val categoryBudgetRef = FirebaseFirestore.getInstance()
            .collection("users").document(userId)
            .collection("categoryBudgets").document(category.name)

        val defaultGoals = mapOf("min" to 0.0, "max" to 0.0)

        categoryBudgetRef.set(defaultGoals, SetOptions.merge())
    }

}