package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import vc.prog3c.poe.databinding.ActivityCompleteProfileBinding
import vc.prog3c.poe.ui.viewmodels.LoginViewModel

class CompleteProfileView : AppCompatActivity() {
    private lateinit var binding: ActivityCompleteProfileBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompleteProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        setupToolbar()
        setupProfilePicture()
        setupCardTypeSpinner()
        setupSaveButton()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupProfilePicture() {
        binding.addProfilePicButton.setOnClickListener {
            // Handle profile picture selection
        }
    }

    private fun setupCardTypeSpinner() {
        val cardTypes = arrayOf("Visa", "Mastercard", "American Express")
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cardTypes)
        binding.cardTypeSpinner.setAdapter(adapter)
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            val address = binding.accountEditText.text.toString()
            val phone = binding.phoneNumberEditText.text.toString()
            val cardNumber = binding.cardNumberEditText.text.toString()
            val cardType = binding.cardTypeSpinner.text.toString()
            val cvc = binding.cvcEditText.text.toString()
            val expiry = binding.expiryEditText.text.toString()

            if (address.isNotEmpty() && phone.isNotEmpty() && cardNumber.isNotEmpty() && 
                cardType.isNotEmpty() && cvc.isNotEmpty() && expiry.isNotEmpty()) {
                // TODO: Save profile information
                finish()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 