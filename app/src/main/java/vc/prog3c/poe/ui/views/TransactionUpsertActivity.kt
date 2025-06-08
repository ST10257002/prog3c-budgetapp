package vc.prog3c.poe.ui.views

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.datepicker.MaterialDatePicker
import com.permissionx.guolindev.request.ExplainScope
import com.permissionx.guolindev.request.ForwardScope
import kotlinx.coroutines.launch
import vc.prog3c.poe.R
import vc.prog3c.poe.core.coordinators.ConsentCoordinator
import vc.prog3c.poe.core.models.ConsentBundle
import vc.prog3c.poe.core.models.ConsentUiHost
import vc.prog3c.poe.core.models.ImageResult
import vc.prog3c.poe.core.services.DeviceCaptureService
import vc.prog3c.poe.core.services.DeviceGalleryService
import vc.prog3c.poe.data.models.Category
import vc.prog3c.poe.databinding.ActivityTransactionUpsertBinding
import vc.prog3c.poe.ui.viewmodels.TransactionUpsertUiState
import vc.prog3c.poe.ui.viewmodels.TransactionUpsertViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionUpsertActivity : AppCompatActivity(), View.OnClickListener,
    View.OnLongClickListener, ConsentUiHost {
    companion object {
        private const val TAG = "TransactionUpsertActivity"
    }

    private lateinit var binds: ActivityTransactionUpsertBinding
    private lateinit var model: TransactionUpsertViewModel

    private lateinit var captureService: DeviceCaptureService
    private lateinit var galleryService: DeviceGalleryService
    private var selectedPhotoUri: String? = null
    private var accountId: String? = null

    // --- Lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBindings()
        setupLayoutUi()

        model = ViewModelProvider(this)[TransactionUpsertViewModel::class.java]

        captureService = DeviceCaptureService(this)
        galleryService = DeviceGalleryService(this)

        setupClickListeners()
        configureCaptureEventHandler()
        configureGalleryEventHandler()
        loadOptionsForVariantsDropdown()

        accountId = intent.getStringExtra("account_id")
        if (accountId == null) {
            Toast.makeText(
                this, "Account ID is required", Toast.LENGTH_SHORT
            ).show()
            finish()
        } else {
            model.loadAccountName(accountId!!)
            model.loadCategories()
        }

        requestPermissions()
        observeViewModelState()
        observeViewModelValue()
    }

    // --- ViewModel


    private fun observeViewModelState() = model.uiState.observe(this) { state ->
        when (state) {
            is TransactionUpsertUiState.Success -> {
                Toast.makeText(
                    this, state.message, Toast.LENGTH_SHORT
                ).show()
                setResult(RESULT_OK) // Contract to refresh view
                finish()
            }

            is TransactionUpsertUiState.Failure -> {
                Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
            }

            TransactionUpsertUiState.Loading -> {}
        }
    }

    private fun observeViewModelValue() {
        model.accountName.observe(this) { name ->
            supportActionBar?.subtitle = name ?: ""
        }

        model.categories.observe(this) { list ->
            loadOptionsForCategoryDropdown(list)
        }
    }

    // --- Internals

    fun setPhotoPath(path: String) {
        selectedPhotoUri = path
    }

    private fun submitTransaction() {
        val amountStr = binds.etCost.text.toString()
        val description = binds.etDescription.text.toString()
        val dateText = binds.etDate.text.toString()
        val category = binds.opCategory.text.toString()
        val variants = binds.opVariants.text.toString()

        lifecycleScope.launch {
            model.saveTransaction(
                amountStr = amountStr,
                description = description,
                category = category,
                variant = variants,
                dateText = dateText,
                accountId = accountId!!,
                photoPath = selectedPhotoUri
            )
        }
    }

    private fun loadImageInView(path: String) {
        setPhotoPath(path)
        val uri = path.toUri()
        Glide.with(binds.ivImage.context)
            .load(uri)
            .centerCrop()
            .into(binds.ivImage)
    }

    private fun loadOptionsForCategoryDropdown(options: List<Category>) {
        binds.opCategory.setAdapter(
            ArrayAdapter(
                this, android.R.layout.simple_dropdown_item_1line, options.map { it.name })
        )
    }

    private fun loadOptionsForVariantsDropdown() {
        val options = arrayOf("Income", "Expense")
        binds.opVariants.setAdapter(
            ArrayAdapter(
                this, android.R.layout.simple_dropdown_item_1line, options
            )
        )
    }

    private fun requestPermissions() {
        ConsentCoordinator.requestConsent(
            this, this, consentBundles = arrayOf(
                ConsentBundle.CameraAccess, ConsentBundle.ImageLibraryAccess
            )
        )
    }

    // --- Dialog

    private fun showDateSelectorDialog() {
        val dialog = MaterialDatePicker.Builder.datePicker().apply {
            setTitleText("When did the transaction happen?")
        }.build()

        dialog.addOnPositiveButtonClickListener { date ->
            val formatted = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(date))
            binds.etDate.setText(formatted)
        }

        dialog.show(
            supportFragmentManager, "date_picker"
        )
    }

    // --- Event Handlers (Capture)

    private fun configureCaptureEventHandler() {
        captureService.registerForLauncherResult { result ->
            if (result is ImageResult.Success) loadImageInView(result.fileUri.toString())
        }
    }

    private fun configureGalleryEventHandler() {
        galleryService.registerForLauncherResult { result ->
            if (result is ImageResult.Success) loadImageInView(result.fileUri.toString())
        }
    }

    // --- Event Handlers (Consent)

    override fun onShowInitialConsentUi(
        scope: ExplainScope, declinedTemporarily: List<String>
    ) {
        scope.showRequestReasonDialog(
            permissions = declinedTemporarily,
            getString(R.string.permx_explain_scope_description),
            getString(R.string.permx_explain_scope_on_positive),
            getString(R.string.permx_explain_scope_on_negative)
        )
    }

    override fun onShowWarningConsentUi(
        scope: ForwardScope, declinedPermanently: List<String>
    ) {
        scope.showForwardToSettingsDialog(
            permissions = declinedPermanently,
            getString(R.string.permx_forward_scope_description),
            getString(R.string.permx_forward_scope_on_positive),
            getString(R.string.permx_forward_scope_on_negative)
        )
    }

    override fun onConsentsAccepted(accepted: List<String>) {
        binds.btImageCapture.isEnabled = true
        binds.btImageGallery.isEnabled = true
        Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show()
    }

    override fun onConsentsDeclined(declined: List<String>) {
        Toast.makeText(this, "Some features require permissions", Toast.LENGTH_SHORT).show()
        finish()
    }

    // --- Event Handlers (Layouts)

    override fun onClick(view: View?) {
        when (view?.id) {
            binds.btImageCapture.id -> captureService.launchCamera()
            binds.btImageGallery.id -> galleryService.launchPicker()
            binds.etDate.id -> showDateSelectorDialog()
            binds.btSave.id -> submitTransaction()
            binds.btnRemoveImage.id -> {
                selectedPhotoUri = null
                binds.ivImage.setImageDrawable(null)
                Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onLongClick(view: View?): Boolean {
        // No longer used for image removal
        return false
    }

    private fun setupClickListeners() {
        binds.btImageCapture.setOnClickListener(this)
        binds.btImageGallery.setOnClickListener(this)
        binds.btSave.setOnClickListener(this)
        binds.etDate.setOnClickListener(this)
        binds.btnRemoveImage.setOnClickListener {
            selectedPhotoUri = null
            binds.ivImage.setImageDrawable(null)
            Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show()
        }
    }

    // --- UI Configuration

    private fun setupToolbar() {
        setSupportActionBar(binds.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Create Transaction"
    }

    // --- UI Registration

    private fun setupBindings() {
        binds = ActivityTransactionUpsertBinding.inflate(layoutInflater)
    }

    private fun setupLayoutUi() {
        enableEdgeToEdge()
        setContentView(binds.root)
        
        // Handle system insets
        ViewCompat.setOnApplyWindowInsetsListener(binds.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                insets.left,
                view.paddingTop,
                insets.right,
                insets.bottom
            )
            windowInsets
        }

        binds.btImageCapture.isEnabled = false
        binds.btImageGallery.isEnabled = false
        setupToolbar()
    }
}
