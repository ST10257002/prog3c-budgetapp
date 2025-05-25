package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import vc.prog3c.poe.databinding.ActivityProfileBinding
import vc.prog3c.poe.ui.viewmodels.AuthViewModel

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupToolbar()
        setupProfileInfo()
        setupButtons()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Profile"
    }

    private fun setupProfileInfo() {
        // TODO: Backend Implementation Required
        // 1. Load user profile data from Firestore
        // 2. Display user's name, email, and profile picture
        // 3. Show account creation date
        // 4. Display last login time
    }

    private fun setupButtons() {
        binding.manageGoalsButton.setOnClickListener {
            startActivity(Intent(this, ManageGoalsView::class.java))
        }

        binding.logoutButton.setOnClickListener {
            // TODO: Backend Implementation Required
            // 1. Sign out user from Firebase Auth
            // 2. Clear local user data
            // 3. Update lastLogin timestamp in Firestore
            // 4. Handle offline state during logout
            startActivity(Intent(this, SignInActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
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