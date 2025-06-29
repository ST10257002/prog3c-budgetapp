package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import vc.prog3c.poe.core.services.AuthService
import vc.prog3c.poe.data.models.Category
import vc.prog3c.poe.data.models.CategoryType
/**
 * @reference Firebase Firestore - Read and Write Data: https://firebase.google.com/docs/firestore/manage-data/add-data
 * @reference Firebase Firestore - Delete Documents: https://firebase.google.com/docs/firestore/manage-data/delete-data
 */

class CategoryViewModel(
    private val authService: AuthService = AuthService(),
    private val dataService: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    init {
        loadCategories()
    }
    
    private fun loadCategories() = viewModelScope.launch {
        try {
            val userId = authService.getCurrentUser()?.uid ?: return@launch
            val userCategories =
                dataService.collection("users").document(userId).collection("categories").get().await()
                    .toObjects(Category::class.java)
            _categories.value = userCategories
        } catch (e: Exception) {
            _error.value = "Failed to load categories: ${e.message}"
        }
    }

    fun addCategory(category: Category) = viewModelScope.launch {
        try {
            val userId = authService.getCurrentUser()?.uid ?: return@launch
            val categoryRef =
                dataService.collection("users").document(userId).collection("categories").document()

            val newCategory = category.copy(id = categoryRef.id)
            categoryRef.set(newCategory).await()

            // Update local categories list
            val currentCategories = _categories.value?.toMutableList() ?: mutableListOf()
            currentCategories.add(newCategory)
            _categories.value = currentCategories
        } catch (e: Exception) {
            _error.value = "Failed to add category: ${e.message}"
        }
    }

    fun deleteCategory(categoryId: String) = viewModelScope.launch {
        try {
            val userId = authService.getCurrentUser()?.uid ?: return@launch
            dataService.collection("users").document(userId).collection("categories").document(categoryId)
                .delete().await()

            // Update local categories list
            val currentCategories = _categories.value?.toMutableList() ?: mutableListOf()
            currentCategories.removeIf { it.id == categoryId }
            _categories.value = currentCategories
        } catch (e: Exception) {
            _error.value = "Failed to delete category: ${e.message}"
        }
    }

    fun updateCategory(category: Category) = viewModelScope.launch {
        try {
            val userId = authService.getCurrentUser()?.uid ?: return@launch
            dataService.collection("users").document(userId).collection("categories").document(category.id)
                .set(category).await()

            // Update local categories list
            val currentCategories = _categories.value?.toMutableList() ?: mutableListOf()
            val index = currentCategories.indexOfFirst { it.id == category.id }
            if (index != -1) {
                currentCategories[index] = category
                _categories.value = currentCategories
            }
        } catch (e: Exception) {
            _error.value = "Failed to update category: ${e.message}"
        }
    }
} 