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
import vc.prog3c.poe.ui.adapters.PhotoAdapter
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import java.util.Date
import java.util.Calendar
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import android.content.Intent
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.app.AlertDialog
import com.google.android.material.datepicker.MaterialDatePicker
import androidx.core.content.FileProvider
import androidx.core.app.ActivityCompat
import com.google.android.gms.tasks.OnSuccessListener
import android.os.Environment
import android.util.Log
import java.io.IOException

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binds: ActivityAddTransactionBinding
    private lateinit var model: TransactionViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var dashboardViewModel: DashboardViewModel
    private var accountId: String? = null
    private var selectedCategory: Category? = null
    private var selectedDate: Date = Date()
    private var currentTransactionType: TransactionType = TransactionType.EXPENSE
    private lateinit var photoAdapter: PhotoAdapter
    private var currentPhotoFile: File? = null
    private val db = FirebaseFirestore.getInstance()
    private lateinit var storage: FirebaseStorage
    private var selectedPhotos = mutableListOf<Uri>()
    private var photoURI: Uri? = null
    private var currentPhotoPath: String = ""

    private val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    private val REQUEST_IMAGE_CAPTURE = 1

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            showPermissionDeniedDialog()
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedPhotos.add(it)
            photoAdapter.updatePhotos(selectedPhotos)
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // The photo was taken successfully
            // The URI is already added to selectedPhotos when launching the camera
        }
    }

    companion object {
        private const val TAG = "AddTransactionActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binds = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binds.root)
        ViewCompat.setOnApplyWindowInsetsListener(binds.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        model = ViewModelProvider(this)[TransactionViewModel::class.java]
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
        setSupportActionBar(binds.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Add Transaction"
    }

    private fun setupPhotoRecyclerView() {
        photoAdapter = PhotoAdapter(
            onPhotoClick = { uri ->
                // Launch PhotoViewerActivity when a photo is clicked
                val intent = Intent(this, PhotoViewerActivity::class.java).apply {
                    putExtra(PhotoViewerActivity.EXTRA_PHOTO_URI, uri.toString())
                }
                startActivity(intent)
            },
            onRemoveClick = { uri ->
                selectedPhotos.remove(uri)
                photoAdapter.updatePhotos(selectedPhotos)
            }
        )
        
        // Set up photo RecyclerView for both forms
        binds.incomeForm.photoRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AddTransactionActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = photoAdapter
        }
        
        binds.expenseForm.photoRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AddTransactionActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = photoAdapter
        }
    }

    private fun setupTransactionTypeDropdown() {
        val transactionTypes = arrayOf("Income", "Expense")
        val adapter = ArrayAdapter(this, R.layout.item_dropdown, transactionTypes)
        binds.transactionTypeDropdown.setAdapter(adapter)

        binds.transactionTypeDropdown.setOnItemClickListener { _, _, position, _ ->
            currentTransactionType = when (position) {
                0 -> TransactionType.INCOME
                1 -> TransactionType.EXPENSE
                else -> TransactionType.EXPENSE
            }
            updateCategoriesForType(currentTransactionType)
            when (position) {
                0 -> showIncomeForm()
                1 -> showExpenseForm()
            }
        }
    }

    private fun showIncomeForm() {
        currentTransactionType = TransactionType.INCOME
        binds.incomeForm.root.visibility = View.VISIBLE
        binds.expenseForm.root.visibility = View.GONE
        updateCategoriesForType(TransactionType.INCOME)
        // Update photo RecyclerView visibility
        binds.incomeForm.photoRecyclerView.visibility = if (selectedPhotos.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun showExpenseForm() {
        currentTransactionType = TransactionType.EXPENSE
        binds.incomeForm.root.visibility = View.GONE
        binds.expenseForm.root.visibility = View.VISIBLE
        updateCategoriesForType(TransactionType.EXPENSE)
        // Update photo RecyclerView visibility
        binds.expenseForm.photoRecyclerView.visibility = if (selectedPhotos.isEmpty()) View.GONE else View.VISIBLE
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
            
            // Update both income source and expense category dropdowns
            binds.incomeForm.sourceInput.setAdapter(adapter)
            binds.incomeForm.sourceInput.setOnItemClickListener { _, _, position, _ ->
                selectedCategory = filteredCategories[position]
            }

            binds.expenseForm.categoryInput.setAdapter(adapter)
            binds.expenseForm.categoryInput.setOnItemClickListener { _, _, position, _ ->
                selectedCategory = filteredCategories[position]
            }
        }
    }

    private fun setupDatePickers() {
        // Income form date picker
        binds.incomeForm.dateInput.setOnClickListener {
            showDatePicker()
        }

        // Expense form time pickers
        binds.expenseForm.startTimeInput.setOnClickListener {
            showTimePicker { time ->
                binds.expenseForm.startTimeInput.setText(time.formatToString())
            }
        }

        binds.expenseForm.endTimeInput.setOnClickListener {
            showTimePicker { time ->
                binds.expenseForm.endTimeInput.setText(time.formatToString())
            }
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setSelection(selectedDate.time)
            .build()

        datePicker.addOnPositiveButtonClickListener { timestamp ->
            selectedDate = Date(timestamp)
            updateDateDisplay()
        }

        datePicker.show(supportFragmentManager, "date_picker")
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
        // Set up photo buttons for both forms
        binds.incomeForm.capturePhotoButton.setOnClickListener {
            checkCameraPermission()
        }
        
        binds.incomeForm.addPhotoButton.setOnClickListener {
            showPhotoOptionsDialog()
        }
        
        binds.expenseForm.capturePhotoButton.setOnClickListener {
            checkCameraPermission()
        }
        
        binds.expenseForm.addPhotoButton.setOnClickListener {
            showPhotoOptionsDialog()
        }
    }

    private fun showPhotoOptionsDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Add Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            ) -> {
                showPermissionRationaleDialog()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Camera Permission Required")
            .setMessage("This app needs camera permission to take photos for transactions.")
            .setPositiveButton("Grant Permission") { _, _ ->
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Denied")
            .setMessage("Camera permission is required to take photos. Please grant it in Settings.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun launchCamera() {
        val photoFile = try {
            createImageFile()
        } catch (ex: IOException) {
            Log.e(TAG, "Error creating image file", ex)
            null
        }

        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                it
            )
            //cameraLauncher.launch(photoURI)
        }
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun setupSubmitButtons() {
        // Income form submit
        binds.incomeForm.submitButton.setOnClickListener {
            if (validateIncomeForm()) {
                submitIncomeTransaction()
            }
        }

        // Expense form submit
        binds.expenseForm.submitButton.setOnClickListener {
            if (validateExpenseForm()) {
                submitExpenseTransaction()
            }
        }
    }

    private fun validateIncomeForm(): Boolean {
        val amount = binds.incomeForm.amountInput.text.toString().toDoubleOrNull()
        val description = binds.incomeForm.descriptionInput.text.toString()
        val category = selectedCategory

        when {
            amount == null || amount <= 0 -> {
                showErrorMessage("Please enter a valid amount")
                return false
            }
            description.isBlank() -> {
                showErrorMessage("Please enter a description")
                return false
            }
            category == null -> {
                showErrorMessage("Please select a category")
                return false
            }
        }
        return true
    }

    private fun validateExpenseForm(): Boolean {
        val amount = binds.expenseForm.amountInput.text.toString().toDoubleOrNull()
        val description = binds.expenseForm.descriptionInput.text.toString()
        val category = selectedCategory
        val startTime = binds.expenseForm.startTimeInput.text.toString()
        val endTime = binds.expenseForm.endTimeInput.text.toString()

        when {
            amount == null || amount <= 0 -> {
                showErrorMessage("Please enter a valid amount")
                return false
            }
            description.isBlank() -> {
                showErrorMessage("Please enter a description")
                return false
            }
            category == null -> {
                showErrorMessage("Please select a category")
                return false
            }
            startTime.isBlank() -> {
                showErrorMessage("Please select start time")
                return false
            }
            endTime.isBlank() -> {
                showErrorMessage("Please select end time")
                return false
            }
        }
        return true
    }

    private fun submitIncomeTransaction() {
        if (!validateIncomeForm()) return

        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            type = TransactionType.INCOME,
            amount = binds.incomeForm.amountInput.text.toString().toDoubleOrNull() ?: 0.0,
            description = binds.incomeForm.descriptionInput.text.toString(),
            date = Timestamp(selectedDate),
            category = selectedCategory?.name ?: "",
            accountId = accountId ?: "",
            userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
            photoUrls = emptyList()
        )

        if (selectedPhotos.isNotEmpty()) {
            uploadPhotosAndSaveTransaction(transaction)
        } else {
            model.addTransaction(accountId ?: "", transaction)
        }
    }

    private fun submitExpenseTransaction() {
        if (!validateExpenseForm()) return

        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            type = TransactionType.EXPENSE,
            amount = binds.expenseForm.amountInput.text.toString().toDoubleOrNull() ?: 0.0,
            description = binds.expenseForm.descriptionInput.text.toString(),
            date = Timestamp(selectedDate),
            category = selectedCategory?.name ?: "",
            accountId = accountId ?: "",
            userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
            photoUrls = emptyList()
        )

        if (selectedPhotos.isNotEmpty()) {
            uploadPhotosAndSaveTransaction(transaction)
        } else {
            model.addTransaction(accountId ?: "", transaction)
        }
    }

    private fun uploadPhotosAndSaveTransaction(transaction: Transaction) {
        val transactionId = transaction.id
        val photoUrls = mutableListOf<String>()
        val uploadTasks = selectedPhotos.map { uri ->
            val photoRef = storage.reference.child("transactions/$transactionId/${UUID.randomUUID()}")
            photoRef.putFile(uri).continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                photoRef.downloadUrl
            }
        }

        Tasks.whenAll(uploadTasks)
            .addOnSuccessListener {
                val downloadUrls = uploadTasks.mapNotNull { it.result }
                photoUrls.addAll(downloadUrls.map { it.toString() })
                
                // Now save transaction with photo URLs
                val transactionWithPhotos = transaction.copy(photoUrls = photoUrls)
                model.addTransaction(accountId ?: "", transactionWithPhotos)
            }
            .addOnFailureListener { e ->
                showErrorMessage("Failure uploading photos: ${e.message}")
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
        Snackbar.make(binds.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showErrorMessage(message: String) {
        Snackbar.make(binds.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun observeViewModels() {
        model.transactionState.observe(this) { state ->
            when (state) {
                is TransactionState.Success -> {
                    binds.loadingOverlay.visibility = View.GONE
                    showSuccessMessage(state.message)
                    setResult(RESULT_OK)
                    finish()
                }
                is TransactionState.Error -> {
                    binds.loadingOverlay.visibility = View.GONE
                    showErrorMessage(state.message)
                }
                TransactionState.Loading -> {
                    binds.loadingOverlay.visibility = View.VISIBLE
                }
            }
        }

        categoryViewModel.categories.observe(this) { categories ->
            updateCategoriesForType(currentTransactionType)
        }
    }

    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        binds.incomeForm.dateInput.setText(dateFormat.format(selectedDate))
    }

    private fun saveTransaction(transaction: Transaction) {
        val transactionRef = db.collection("transactions").document()
        val transactionId = transactionRef.id

        // First upload photos if any
        if (selectedPhotos.isNotEmpty()) {
            val photoUrls = mutableListOf<String>()
            val uploadTasks = selectedPhotos.map { uri ->
                val photoRef = storage.reference.child("transactions/$transactionId/${UUID.randomUUID()}")
                photoRef.putFile(uri).continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    photoRef.downloadUrl
                }
            }

            Tasks.whenAll(uploadTasks)
                .addOnSuccessListener {
                    val downloadUrls = uploadTasks.mapNotNull { it.result }
                    photoUrls.addAll(downloadUrls.map { it.toString() })
                    
                    // Now save transaction with photo URLs
                    val transactionWithPhotos = transaction.copy(photoUrls = photoUrls)
                    transactionRef.set(transactionWithPhotos)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Transaction saved successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failure saving transaction: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failure uploading photos: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Save transaction without photos
            transactionRef.set(transaction)
                .addOnSuccessListener {
                    Toast.makeText(this, "Transaction saved successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failure saving transaction: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
} 