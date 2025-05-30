package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import vc.prog3c.poe.databinding.ActivityCompleteProfileBinding
import vc.prog3c.poe.ui.viewmodels.AuthViewModel

class CompleteProfileView : AppCompatActivity(), View.OnClickListener {

    private lateinit var binds: ActivityCompleteProfileBinding
    private lateinit var model: AuthViewModel // TODO: <-- This needs to change as this viewmodel is no longer used and needs deleting


    // --- Lifecycle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBindings()
        setupLayoutUi()
        setupClickListeners()

        model = ViewModelProvider(this)[AuthViewModel::class.java]

        observeViewModel()
    }


    // --- ViewModel


    private fun observeViewModel() {
        model.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }


    // --- Internals


    private fun saveForm() {
        val address = binds.accountEditText.text.toString()
        val phone = binds.phoneNumberEditText.text.toString()
        val cardNumber = binds.cardNumberEditText.text.toString()
        val cardType = binds.cardTypeSpinner.text.toString()
        val cvc = binds.cvcEditText.text.toString()
        val expiry = binds.expiryEditText.text.toString()

        if (address.isNotEmpty() && phone.isNotEmpty() && cardNumber.isNotEmpty() && cardType.isNotEmpty() && cvc.isNotEmpty() && expiry.isNotEmpty()) {
            // TODO: Save profile information
            startActivity(Intent(this, GoalSettingView::class.java))
            finish()
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }


    // --- Event Handlers


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            binds.addProfilePicButton.id -> {} // TODO: Handle profile picture selection
            binds.saveButton.id -> saveForm()
        }
    }


    private fun setupClickListeners() {
        binds.addProfilePicButton.setOnClickListener(this)
        binds.saveButton.setOnClickListener(this)
    }


    // --- UI Configuration


    private fun setupToolbar() {
        setSupportActionBar(binds.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    private fun setupCardTypeSpinner() {
        val cardTypes = arrayOf("Visa", "Mastercard", "American Express")
        val adapter = android.widget.ArrayAdapter(
            this, android.R.layout.simple_dropdown_item_1line, cardTypes
        )

        binds.cardTypeSpinner.setAdapter(adapter)
    }


    // --- UI


    private fun setupBindings() {
        binds = ActivityCompleteProfileBinding.inflate(layoutInflater)
    }


    private fun setupLayoutUi() {
        setContentView(binds.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binds.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        setupCardTypeSpinner()
        setupToolbar()
    }
} 