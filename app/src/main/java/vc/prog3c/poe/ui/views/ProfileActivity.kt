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
import vc.prog3c.poe.R
import vc.prog3c.poe.databinding.ActivityProfileBinding
import vc.prog3c.poe.ui.viewmodels.AuthViewModel

class ProfileActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binds: ActivityProfileBinding
    private lateinit var model: AuthViewModel


    // --- Lifecycle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBindings()
        setupLayoutUi()
        setupClickListeners()

        model = ViewModelProvider(this)[AuthViewModel::class.java]

        setupBottomNavigation()
        observeViewModel()

        model.loadUserProfile() // Load user info from Firestore
    }


    override fun onResume() {
        super.onResume() // Ensure profile is selected when returning to this activity
        binds.bottomNavigation.selectedItemId = R.id.nav_profile
    }


    // --- ViewModel


    private fun observeViewModel() {
        model.currentUser.observe(this) { user ->
            user?.let {
                binds.nameText.text = it.name
                binds.emailText.text = it.email
                binds.addressText.text = it.address

                // Optionally load profile picture if using Coil/Glide
                // Glide.with(this).load(it.profilePictureUrl).into(vBinds.profileImage)
            }
        }

        model.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }


    // --- Internals


    private fun setupBottomNavigation() {
        binds.bottomNavigation.setOnItemSelectedListener { item ->
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

        binds.bottomNavigation.selectedItemId = R.id.nav_profile
    }


    // --- Event Handlers


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            binds.manageGoalsButton.id -> startActivity(
                Intent(
                    this, ManageGoalsActivity::class.java
                )
            )

            binds.achievementsButton.id -> {
                val intent = Intent(this, AchievementsActivity::class.java)
                startActivity(intent)
            }

            binds.logoutButton.id -> {
                model.signOut()
                startActivity(Intent(this, SignInActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
        }
    }


    private fun setupClickListeners() {
        binds.manageGoalsButton.setOnClickListener(this)
        binds.achievementsButton.setOnClickListener(this)
        binds.logoutButton.setOnClickListener(this)
    }


    // --- UI Configuration


    private fun setupToolbar() {
        setSupportActionBar(binds.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Profile"
    }


    // --- UI


    private fun setupBindings() {
        binds = ActivityProfileBinding.inflate(layoutInflater)
    }


    private fun setupLayoutUi() {
        setContentView(binds.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binds.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        setupToolbar()
    }
}
