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

    private lateinit var vBinds: ActivityCompleteProfileBinding
    private lateinit var vModel: AuthViewModel // TODO: <-- This needs to change as this viewmodel is no longer used and needs deleting


    // --- Lifecycle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBindings()
        setupLayoutUi()
        setupClickListeners()

        vModel = ViewModelProvider(this)[AuthViewModel::class.java]

        observeViewModel()
    }


    // --- ViewModel


    private fun observeViewModel() {
        vModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }


    // --- Internals


    private fun saveForm() {
        val address = vBinds.accountEditText.text.toString()
        val phone = vBinds.phoneNumberEditText.text.toString()
        val cardNumber = vBinds.cardNumberEditText.text.toString()
        val cardType = vBinds.cardTypeSpinner.text.toString()
        val cvc = vBinds.cvcEditText.text.toString()
        val expiry = vBinds.expiryEditText.text.toString()

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
            vBinds.addProfilePicButton.id -> {} // TODO: Handle profile picture selection
            vBinds.saveButton.id -> saveForm()
        }
    }


    private fun setupClickListeners() {
        vBinds.addProfilePicButton.setOnClickListener(this)
        vBinds.saveButton.setOnClickListener(this)
    }


    // --- UI Configuration


    private fun setupToolbar() {
        setSupportActionBar(vBinds.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    private fun setupCardTypeSpinner() {
        val cardTypes = arrayOf("Visa", "Mastercard", "American Express")
        val adapter = android.widget.ArrayAdapter(
            this, android.R.layout.simple_dropdown_item_1line, cardTypes
        )

        vBinds.cardTypeSpinner.setAdapter(adapter)
    }


    // --- UI


    private fun setupBindings() {
        vBinds = ActivityCompleteProfileBinding.inflate(layoutInflater)
    }


    private fun setupLayoutUi() {
        setContentView(vBinds.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(vBinds.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        setupCardTypeSpinner()
        setupToolbar()
    }
} 