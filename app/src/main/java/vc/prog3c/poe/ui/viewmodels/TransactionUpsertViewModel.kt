package vc.prog3c.poe.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import vc.prog3c.poe.core.services.AuthService
import vc.prog3c.poe.data.models.Category
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import vc.prog3c.poe.data.repository.TransactionRepository
import vc.prog3c.poe.data.services.FirestoreService
import vc.prog3c.poe.ui.viewmodels.TransactionUpsertState
import java.net.URL
import java.util.UUID

class TransactionUpsertViewModel(
    private val authService: AuthService = AuthService(),
    private val firestoreService: FirestoreService = FirestoreService
) : ViewModel() {

    val accountName = MutableLiveData<String>()
    val categories = MutableLiveData<List<Category>>()
    val state = MutableLiveData<TransactionUpsertState>()

    private var selectedPhotoUri: String? = null

    fun loadAccountName(accountId: String) {
        firestoreService.account.getAccount(accountId) { result ->
            accountName.value = result?.name ?: "Unknown Account"
        }
    }

    fun loadCategories() {
        firestoreService.category.getAllCategories { result ->
            categories.value = result ?: emptyList()
        }
    }

    fun saveTransaction(
        amountStr: String,
        description: String,
        category: String,
        variant: String,
        dateText: String,
        accountId: String,
        photoPath: String? = null
    ) {
        val userId = authService.getCurrentUser()?.uid
        if (userId == null) {
            state.value = TransactionUpsertState.Error("User not authenticated")
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            state.value = TransactionUpsertState.Error("Invalid amount")
            return
        }

        selectedPhotoUri = photoPath
        Log.d("PHOTO","$selectedPhotoUri")
        Log.d("PHOTO","$photoPath")

        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            type = TransactionType.valueOf(variant.uppercase()),
            amount = amount,
            description = description,
            category = category,
            date = Timestamp.now(), // You can parse dateText here if needed
            accountId = accountId,
            userId = userId,
            photoUrls = selectedPhotoUri?.let { listOf(it) } ?: emptyList()
        )

        state.value = TransactionUpsertState.Loading
        firestoreService.transaction.addTransaction(transaction) { success ->
            state.value = if (success) {
                TransactionUpsertState.Success("Transaction saved!")
            } else {
                TransactionUpsertState.Error("Failed to save transaction")
            }
        }
    }
}

