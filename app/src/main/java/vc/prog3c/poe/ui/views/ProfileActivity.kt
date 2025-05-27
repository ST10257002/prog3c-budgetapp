package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import vc.prog3c.poe.databinding.ActivityProfileBinding
import vc.prog3c.poe.ui.viewmodels.AuthViewModel

class ProfileActivity : AppCompatActivity() {

    private lateinit var vBinds: ActivityProfileBinding
    private lateinit var vModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vBinds = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(vBinds.root)

        vModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupToolbar()
        setupButtons()
        observeViewModel()

        vModel.loadUserProfile() // Load user info from Firestore
    }

    private fun setupToolbar() {
        setSupportActionBar(vBinds.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Profile"
    }

    private fun setupButtons() {
        vBinds.manageGoalsButton.setOnClickListener {
            startActivity(Intent(this, ManageGoalsView::class.java))
        }

        vBinds.logoutButton.setOnClickListener {
            vModel.signOut()
            startActivity(Intent(this, SignInActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    private fun observeViewModel() {
        vModel.currentUser.observe(this) { user ->
            user?.let {
                vBinds.nameText.text = it.name
                vBinds.emailText.text = it.email
                vBinds.addressText.text = it.address

                // Optionally load profile picture if using Coil/Glide
                // Glide.with(this).load(it.profilePictureUrl).into(vBinds.profileImage)
            }
        }

        vModel.error.observe(this) { error ->
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
