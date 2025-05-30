package vc.prog3c.poe.ui.views

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.OnFailureListener
import vc.prog3c.poe.R
import vc.prog3c.poe.data.models.Transaction
import vc.prog3c.poe.data.models.TransactionType
import vc.prog3c.poe.data.models.Category
import vc.prog3c.poe.data.models.CategoryType
import vc.prog3c.poe.databinding.ActivityAddTransactionBinding
import vc.prog3c.poe.ui.viewmodels.TransactionState
import vc.prog3c.poe.ui.viewmodels.TransactionViewModel
import vc.prog3c.poe.ui.viewmodels.CategoryViewModel
import vc.prog3c.poe.ui.viewmodels.DashboardViewModel
import vc.prog3c.poe.adapters.PhotoAdapter
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import java.util.Date
import java.util.Calendar
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import vc.prog3c.poe.utils.ImageUtils
import java.io.File

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var viewModel: TransactionViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var dashboardViewModel: DashboardViewModel
    private var accountId: String? = null
    private var selectedCategory: Category? = null
    private var selectedDate: Date = Date()
    private var currentTransactionType: TransactionType = TransactionType.EXPENSE
    private lateinit var photoAdapter: PhotoAdapter
    private var currentPhotoFile: File? = null
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // TODO: REPLACE WITH SERVICE
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.CAMERA] == true -> {
                launchCamera()
            }
            permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true -> {
                launchImagePicker()
            }
            else -> {
                Toast.makeText(this, "Permissions required to add photos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // TODO: REPLACE WITH SERVICE
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                photoAdapter.addPhoto(uri)
            }
        }
    }

    // TODO: REPLACE WITH SERVICE
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            currentPhotoFile?.let { file ->
                val uri = ImageUtils.getUriFromFile(this, file)
                photoAdapter.addPhoto(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[TransactionViewModel::class.java]
        categoryViewModel = ViewModelProvider(this)[CategoryViewModel::class.java]
        dashboardViewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        accountId = intent.getStringExtra("account_id")

        if (accountId == null) {
            Toast.makeText(this, "Account ID is required", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupPhotoRecyclerView()
        setupTransactionTypeDropdown()
        setupCategoryDropdown()
        setupDatePickers()
        setupPhotoHandling()
        setupSubmitButtons()
        observeViewModels()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Add Transaction"
    }

    private fun setupPhotoRecyclerView() {
        photoAdapter = PhotoAdapter { uri ->
            // Handle photo click - you can implement photo preview here
        }
        binding.expenseForm.photoRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AddTransactionActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = photoAdapter
        }
    }

    private fun setupTransactionTypeDropdown() {
        val transactionTypes = arrayOf("Income", "Expense")
        val adapter = ArrayAdapter(this, R.layout.item_dropdown, transactionTypes)
        binding.transactionTypeDropdown.setAdapter(adapter)

        binding.transactionTypeDropdown.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> showIncomeForm()
                1 -> showExpenseForm()
            }
        }
    }

    private fun showIncomeForm() {
        binding.incomeForm.root.visibility = View.VISIBLE
        binding.expenseForm.root.visibility = View.GONE
    }

    private fun showExpenseForm() {
        binding.incomeForm.root.visibility = View.GONE
        binding.expenseForm.root.visibility = View.VISIBLE
    }

    private fun setupCategoryDropdown() {
        categoryViewModel.categories.observe(this) { categories ->
            updateCategoriesForType(currentTransactionType)
        }
    }

    private fun updateCategoriesForType(type: TransactionType) {
        categoryViewModel.categories.value?.let { categories ->
            val filteredCategories = when (type) {
                TransactionType.EXPENSE -> categories.filter { it.type == CategoryType.UTILITIES }
                TransactionType.INCOME -> categories.filter { it.type == CategoryType.SAVINGS }
                TransactionType.EARNED -> categories.filter { it.type == CategoryType.SAVINGS }
                TransactionType.REDEEMED -> categories.filter { it.type == CategoryType.EMERGENCY }
            }
            
            val categoryNames = filteredCategories.map { it.name }
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categoryNames)
            
            binding.expenseForm.categoryInput.setAdapter(adapter)
            binding.expenseForm.categoryInput.setOnItemClickListener { _, _, position, _ ->
                selectedCategory = filteredCategories[position]
            }
        }
    }

    private fun setupDatePickers() {
        // Income form date picker
        binding.incomeForm.dateInput.setOnClickListener {
            showDatePicker { date ->
                binding.incomeForm.dateInput.setText(date.formatToString())
            }
        }

        // Expense form time pickers
        binding.expenseForm.startTimeInput.setOnClickListener {
            showTimePicker { time ->
                binding.expenseForm.startTimeInput.setText(time.formatToString())
            }
        }

        binding.expenseForm.endTimeInput.setOnClickListener {
            showTimePicker { time ->
                binding.expenseForm.endTimeInput.setText(time.formatToString())
            }
        }
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker(onTimeSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                onTimeSelected(calendar.time)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun setupPhotoHandling() {
        binding.expenseForm.addPhotoButton.setOnClickListener {
            checkPermissionAndLaunchImagePicker()
        }

        binding.expenseForm.capturePhotoButton.setOnClickListener {
            checkPermissionAndLaunchCamera()
        }
    }

    private fun setupSubmitButtons() {
        // Income form submit
        binding.incomeForm.submitButton.setOnClickListener {
            if (validateIncomeForm()) {
                submitIncomeTransaction()
            }
        }

        // Expense form submit
        binding.expenseForm.submitButton.setOnClickListener {
            if (validateExpenseForm()) {
                submitExpenseTransaction()
            }
        }
    }

    private fun validateIncomeForm(): Boolean {
        var isValid = true
        
        with(binding.incomeForm) {
            // Validate amount
            if (amountInput.text.isNullOrEmpty()) {
                amountLayout.error = "Amount is required"
                isValid = false
            }

            // Validate source
            if (sourceInput.text.isNullOrEmpty()) {
                sourceLayout.error = "Source is required"
                isValid = false
            }

            // Validate date
            if (dateInput.text.isNullOrEmpty()) {
                dateLayout.error = "Date is required"
                isValid = false
            }
        }

        return isValid
    }

    private fun validateExpenseForm(): Boolean {
        var isValid = true
        
        with(binding.expenseForm) {
            // Validate amount
            if (amountInput.text.isNullOrEmpty()) {
                amountLayout.error = "Amount is required"
                isValid = false
            }

            // Validate category
            if (categoryInput.text.isNullOrEmpty()) {
                categoryLayout.error = "Category is required"
                isValid = false
            }

            // Validate times
            if (startTimeInput.text.isNullOrEmpty()) {
                startTimeLayout.error = "Start time is required"
                isValid = false
            }

            if (endTimeInput.text.isNullOrEmpty()) {
                endTimeLayout.error = "End time is required"
                isValid = false
            }
        }

        return isValid
    }

    private fun submitIncomeTransaction() {
        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            type = TransactionType.INCOME,
            amount = binding.incomeForm.amountInput.text.toString().toDoubleOrNull() ?: 0.0,
            description = binding.incomeForm.descriptionInput.text.toString(),
            date = Timestamp(parseDate(binding.incomeForm.dateInput.text.toString())),
            category = selectedCategory?.name ?: "",
            accountId = accountId ?: "",
            userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        )

        viewModel.addTransaction(transaction)
            .addOnSuccessListener {
                showSuccessMessage("Income transaction added successfully")
                finish()
            }
            .addOnFailureListener { e ->
                showErrorMessage("Failed to add income transaction: ${e.message}")
            }
    }

    private fun submitExpenseTransaction() {
        // First upload photos if any
        val photoUrls = mutableListOf<String>()
        val uploadTasks = photoAdapter.getPhotos().map { uri ->
            uploadPhoto(uri)
        }

        Tasks.whenAll(uploadTasks)
            .addOnSuccessListener {
                // Get photo URLs
                val downloadUrls = uploadTasks.mapNotNull { uploadTask ->
                    try {
                        uploadTask.result.toString()
                    } catch (e: Exception) {
                        null
                    }
                }
                photoUrls.addAll(downloadUrls)
                
                // Create and submit transaction
                val transaction = Transaction(
                    id = UUID.randomUUID().toString(),
                    type = TransactionType.EXPENSE,
                    amount = binding.expenseForm.amountInput.text.toString().toDoubleOrNull() ?: 0.0,
                    description = binding.expenseForm.descriptionInput.text.toString(),
                    date = Timestamp(Date()),
                    category = selectedCategory?.name ?: "",
                    accountId = accountId ?: "",
                    userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                )

                viewModel.addTransaction(transaction)
                    .addOnSuccessListener {
                        showSuccessMessage("Expense transaction added successfully")
                        finish()
                    }
                    .addOnFailureListener { e ->
                        showErrorMessage("Failed to add expense transaction: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                showErrorMessage("Failed to upload photos: ${e.message}")
            }
    }

    
    private fun uploadPhoto(uri: Uri): Task<Uri> {
        return storage.reference
            .child("transactions/${UUID.randomUUID()}")
            .putFile(uri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                task.result.storage.downloadUrl
            }
    }

    
    // Helper functions for date/time formatting and parsing
    private fun Date.formatToString(): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(this)
    }

    
    private fun parseDate(dateStr: String): Date {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateStr) ?: Date()
    }

    
    private fun parseTime(timeStr: String): Date {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).parse(timeStr) ?: Date()
    }

    
    private fun showSuccessMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
    

    private fun showErrorMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    
    private fun observeViewModels() {
        viewModel.transactionState.observe(this) { state ->
            when (state) {
                is TransactionState.Success -> {
                    binding.loadingOverlay.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }

                is TransactionState.Error -> {
                    binding.loadingOverlay.visibility = View.GONE
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }

                TransactionState.Loading -> {
                    binding.loadingOverlay.visibility = View.VISIBLE
                }
            }
        }
    }

    
    // TODO: REPLACE WITH SERVICE
    private fun checkPermissionAndLaunchImagePicker() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchImagePicker()
            }
            else -> {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
            }
        }
    }

    
    // TODO: REPLACE WITH SERVICE
    private fun checkPermissionAndLaunchCamera() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }
            else -> {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
            }
        }
    }
    

    // TODO: REPLACE WITH SERVICE
    private fun launchImagePicker() {
        imagePickerLauncher.launch(ImageUtils.getImagePickerIntent())
    }
    

    // TODO: REPLACE WITH SERVICE
    private fun launchCamera() {
        currentPhotoFile = ImageUtils.createImageFile(this)
        currentPhotoFile?.let { file ->
            cameraLauncher.launch(ImageUtils.getCameraIntent(this, file))
        }
    }

    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
} 