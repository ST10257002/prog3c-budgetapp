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
import com.bumptech.glide.Glide
import com.permissionx.guolindev.request.ExplainScope
import com.permissionx.guolindev.request.ForwardScope
import vc.prog3c.poe.R
import vc.prog3c.poe.core.coordinators.ConsentCoordinator
import vc.prog3c.poe.core.models.ConsentBundle
import vc.prog3c.poe.core.models.ConsentUiHost
import vc.prog3c.poe.core.models.ImageResult
import vc.prog3c.poe.core.services.DeviceCaptureService
import vc.prog3c.poe.core.services.DeviceGalleryService
import vc.prog3c.poe.databinding.ActivityTransactionUpsertBinding
import vc.prog3c.poe.ui.viewmodels.TransactionUpsertViewModel
import java.io.File

class TransactionUpsertActivity : AppCompatActivity(), View.OnClickListener, ConsentUiHost {
    companion object {
        private const val TAG = "TransactionUpsertActivity"
    }

    private lateinit var binds: ActivityTransactionUpsertBinding
    private lateinit var model: TransactionUpsertViewModel

    private lateinit var captureService: DeviceCaptureService
    private lateinit var galleryService: DeviceGalleryService

    private var accountId: String? = null // TODO: Should be in ViewModel


    // --- Lifecycle


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setupBindings()
        setupLayoutUi()

        model = ViewModelProvider(this)[TransactionUpsertViewModel::class.java]

        captureService = DeviceCaptureService(this)
        galleryService = DeviceGalleryService(this)

        setupClickListeners()
        configureCaptureEventHandler()
        configureGalleryEventHandler()
        loadOptionsForCategoryDropdown()
        loadOptionsForVariantsDropdown()

        accountId = intent.getStringExtra("account_id")
        if (accountId == null) {
            Toast.makeText(
                this, "Account ID is required", Toast.LENGTH_SHORT
            ).show()
            finish()
        }
        
        // Set action bar subtitle to account name
        val name = model.getAccountName(accountId)
        if (name.isNotBlank()) { // TODO: The account repo isn't fetching the account name
            supportActionBar?.subtitle = name
        }

        requestPermissions()
        observeViewModel()
    }


    // --- ViewModel


    private fun observeViewModel() {
        // TODO
    }


    // --- Internals


    private fun submitTransaction() {
        // TODO
    }


    private fun loadImageInView(path: String) {
        val uri = path.toUri()
        Glide.with(binds.ivImage.context).apply {
            load(uri).into(binds.ivImage)
        }
    }


    private fun loadOptionsForCategoryDropdown() {
        val options = model.fetchCategories()
        binds.opCategory.setAdapter(
            ArrayAdapter(
                this, android.R.layout.simple_dropdown_item_1line, options.map {
                    it.name
                })
        )

        // TODO: Currently loading nothing idk (?)
        // TODO: Add method in viewmodel to pull from DB
        // TODO: On-click pass index to save transaction
    }


    private fun loadOptionsForVariantsDropdown() {
        val options = arrayOf("Income", "Expense")
        binds.opVariants.setAdapter(
            ArrayAdapter(
                this, android.R.layout.simple_dropdown_item_1line, options
            )
        )

        // TODO: Literally just income or expense
        // TODO: This is loading, but I have no idea if the model expects an enum etc.
    }


    private fun requestPermissions() {
        ConsentCoordinator.requestConsent(
            this, this, consentBundles = arrayOf(
                ConsentBundle.CameraAccess, ConsentBundle.ImageLibraryAccess
            )
        )
    }


    // --- Event Handlers (Capture)


    private fun configureCaptureEventHandler() {
        captureService.registerForLauncherResult { result ->
            when (result) {
                is ImageResult.Success -> loadImageInView(result.fileUri.toString())
                else -> {
                    // TODO: There are two events that can optionally be accounted for (see: core/models/ImageResult)
                }
            }
        }
    }


    private fun configureGalleryEventHandler() {
        galleryService.registerForLauncherResult { result ->
            when (result) {
                is ImageResult.Success -> loadImageInView(result.fileUri.toString())
                else -> {
                    // TODO: There are two events that can optionally be accounted for (see: core/models/ImageResult)
                }
            }
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
        Toast.makeText(
            this, "Granted", Toast.LENGTH_SHORT
        ).show()
    }


    override fun onConsentsDeclined(declined: List<String>) {
        Toast.makeText(
            this, "Some features require permissions", Toast.LENGTH_SHORT
        ).show()
        finish()
    }


    // --- Event Handlers (Layouts)


    override fun onClick(view: View?) {
        when (view?.id) {
            binds.btImageCapture.id -> captureService.launchCamera()
            binds.btImageGallery.id -> galleryService.launchPicker()
            binds.btSave.id -> submitTransaction()
        }
    }


    private fun setupClickListeners() {
        binds.btImageCapture.setOnClickListener(this)
        binds.btImageGallery.setOnClickListener(this)
        binds.btSave.setOnClickListener(this)
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
        ViewCompat.setOnApplyWindowInsetsListener(binds.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binds.btImageCapture.isEnabled = false
        binds.btImageGallery.isEnabled = false
        setupToolbar()
    }
}