package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.ViewModel
import vc.prog3c.poe.core.services.AuthService
import vc.prog3c.poe.data.models.Category
import vc.prog3c.poe.data.services.FirestoreService

class TransactionUpsertViewModel(
    private val authService: AuthService = AuthService(),
    private val dataService: FirestoreService = FirestoreService
) : ViewModel() {
    
    // --- Activity Functions
    
    
    fun getAccountName(accountId: String?) : String {
        var name: String = ""
        if (accountId != null) {
            dataService.account.getAccount(accountId) { result ->
                name = result?.name.toString()
            }
        }
        
        return name
    }
    
    
    fun fetchCategories(): List<Category> { 
        var collection = mutableListOf<Category>()
        dataService.category.getAllCategories { result ->
            if (!(collection.isEmpty()) && (result != null)) {
                collection = result.toMutableList()
            }
        }
        
        return collection.toList()
    }
}