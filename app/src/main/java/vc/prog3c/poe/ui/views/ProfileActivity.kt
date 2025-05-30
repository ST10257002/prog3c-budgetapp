package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import vc.prog3c.poe.R
import vc.prog3c.poe.databinding.ActivityProfileBinding
import vc.prog3c.poe.ui.viewmodels.AuthViewModel

class ProfileActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var vBinds: ActivityProfileBinding
    private lateinit var vModel: AuthViewModel


    // --- Lifecycle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBindings()
        setupLayoutUi()
        setupClickListeners()

        vModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupBottomNavigation()
        observeViewModel()

        vModel.loadUserProfile() // Load user info from Firestore
    }


    override fun onResume() {
        super.onResume() // Ensure profile is selected when returning to this activity
        vBinds.bottomNavigation.selectedItemId = R.id.nav_profile
    }


    // --- ViewModel


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


    // --- Internals


    private fun setupBottomNavigation() {
        vBinds.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, DashboardView::class.java))
                    finish() // Close current activity
                    true
                }

                R.id.nav_accounts -> {
                    startActivity(Intent(this, AccountsView::class.java))
                    finish() // Close current activity
                    true
                }

                R.id.nav_graph -> {
                    startActivity(Intent(this, GraphView::class.java))
                    finish() // Close current activity
                    true
                }

                R.id.nav_profile -> {
                    // Already on profile
                    true
                }

                else -> false
            }
        }

        vBinds.bottomNavigation.selectedItemId = R.id.nav_profile
    }


    // --- Event Handlers


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            vBinds.manageGoalsButton.id -> startActivity(
                Intent(
                    this, ManageGoalsActivity::class.java
                )
            )

            vBinds.achievementsButton.id -> {
                val intent = Intent(this, AchievementsActivity::class.java)
                startActivity(intent)
            }

            vBinds.logoutButton.id -> {
                vModel.signOut()
                startActivity(Intent(this, SignInActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
        }
    }


    private fun setupClickListeners() {
        vBinds.manageGoalsButton.setOnClickListener(this)
        vBinds.achievementsButton.setOnClickListener(this)
        vBinds.logoutButton.setOnClickListener(this)
    }


    // --- UI Configuration


    private fun setupToolbar() {
        setSupportActionBar(vBinds.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Profile"
    }


    // --- UI


    private fun setupBindings() {
        vBinds = ActivityProfileBinding.inflate(layoutInflater)
    }


    private fun setupLayoutUi() {
        setContentView(vBinds.root)
        enableEdgeToEdge()
        setupToolbar()
    }
}
