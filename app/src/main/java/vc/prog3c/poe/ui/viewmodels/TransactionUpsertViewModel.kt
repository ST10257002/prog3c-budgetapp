package vc.prog3c.poe.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import vc.prog3c.poe.core.services.AuthService
import vc.prog3c.poe.core.utils.Blogger
import vc.prog3c.poe.data.models.Category
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import vc.prog3c.poe.data.services.FirestoreService
import vc.prog3c.poe.ui.viewmodels.TransactionUpsertUiState.Failure
import vc.prog3c.poe.ui.viewmodels.TransactionUpsertUiState.Loading
import vc.prog3c.poe.ui.viewmodels.TransactionUpsertUiState.Success
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
/**
 * @reference Android ViewModel + LiveData: https://developer.android.com/topic/libraries/architecture/viewmodel
 * @reference Firebase Firestore - Add Documents with Auto-Generated ID: https://firebase.google.com/docs/firestore/manage-data/add-data#add_a_document
 * @reference Firebase Timestamp: https://firebase.google.com/docs/reference/kotlin/com/google/firebase/Timestamp
 */

class TransactionUpsertViewModel(
    private val authService: AuthService = AuthService(),
    private val dataService: FirestoreService = FirestoreService
) : ViewModel() {
    companion object {
        private const val TAG = "TransactionUpsertViewModel"
    }

    val accountName = MutableLiveData<String>()
    val categories = MutableLiveData<List<Category>>()
    val uiState = MutableLiveData<TransactionUpsertUiState>()

    private var selectedPhotoUri: String? = null

    fun loadAccountName(accountId: String) {
        dataService.account.getAccount(accountId) { result ->
            accountName.value = result?.name ?: "Unknown Account"
        }
    }

    fun loadCategories() {
        dataService.category.getAllCategories { result ->
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
            uiState.value = Failure("User not authenticated")
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            uiState.value = Failure("Invalid amount")
            return
        }

        selectedPhotoUri = photoPath

        Blogger.i(
            TAG, "Storing photo with URI: $selectedPhotoUri"
        )

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val parsedDate = try {
            dateFormat.parse(dateText)?.let {
                Timestamp(it)
            } ?: Timestamp.now()
        } catch (_: Exception) {
            Timestamp.now() // Fallback to current date if parsing fails
        }

        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            type = TransactionType.valueOf(variant.uppercase()),
            amount = amount,
            description = description,
            category = category,
            date = parsedDate,
            accountId = accountId,
            userId = userId,
            imageUri = selectedPhotoUri ?: ""
        )

        uiState.value = Loading
        dataService.transaction.addTransaction(transaction) { success ->
            uiState.value = if (success) {
                Success("Transaction saved!")
            } else {
                Failure("Failed to save transaction")
            }
        }
    }
}

