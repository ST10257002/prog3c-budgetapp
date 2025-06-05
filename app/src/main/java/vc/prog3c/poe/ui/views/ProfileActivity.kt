package vc.prog3c.poe.ui.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import vc.prog3c.poe.R
import vc.prog3c.poe.core.utils.SeedData
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


    private fun showDeleteAccountConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("This will permanently delete your account and all associated data. Continue?")
            .setPositiveButton("Delete") { _, _ -> deleteAccountAndData() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Can move into user repo, or viewmodel
    private fun deleteAccountAndData() {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid

        if (user == null || uid == null) {
            Toast.makeText(this, "No user logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val userDocRef = db.collection("users").document(uid)

        // Step 1: Delete nested transactions under each account
        userDocRef.collection("accounts").get()
            .addOnSuccessListener { accounts ->
                val deletionTasks = mutableListOf<com.google.android.gms.tasks.Task<Void>>()

                for (account in accounts) {
                    val accountId = account.id
                    val txRef = userDocRef.collection("accounts")
                        .document(accountId)
                        .collection("transactions")

                    // Delete each transaction in this account
                    txRef.get().addOnSuccessListener { transactions ->
                        for (transaction in transactions) {
                            deletionTasks.add(txRef.document(transaction.id).delete())
                        }
                    }

                    // Delete the account itself
                    deletionTasks.add(userDocRef.collection("accounts").document(accountId).delete())
                }

                // Step 2: Delete savings goals
                userDocRef.collection("savingsGoals").get()
                    .addOnSuccessListener { goals ->
                        for (goal in goals) {
                            deletionTasks.add(userDocRef.collection("savingsGoals").document(goal.id).delete())
                        }

                        // Step 3: Delete budgets
                        userDocRef.collection("budgets").get()
                            .addOnSuccessListener { budgets ->
                                for (budget in budgets) {
                                    deletionTasks.add(userDocRef.collection("budgets").document(budget.id).delete())
                                }

                                // Step 4: Delete user document
                                deletionTasks.add(userDocRef.delete())

                                // Wait for all deletes, then delete auth account
                                com.google.android.gms.tasks.Tasks.whenAllComplete(deletionTasks)
                                    .addOnSuccessListener {
                                        user.delete().addOnSuccessListener {
                                            Toast.makeText(this, "Account fully deleted.", Toast.LENGTH_SHORT).show()
                                            startActivity(Intent(this, SignInActivity::class.java).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            })
                                            finish()
                                        }.addOnFailureListener {
                                            Toast.makeText(this, "Failed to delete auth account.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete Firestore data.", Toast.LENGTH_SHORT).show()
            }
    }




    override fun onClick(view: View?) {
        when (view?.id) {
            binds.manageGoalsButton.id -> {
                startActivity(Intent(this, ManageGoalsActivity::class.java))
            }

            binds.achievementsButton.id -> {
                startActivity(Intent(this, AchievementsActivity::class.java))
            }

            binds.btnSeedData.id -> {
                val userId = FirebaseAuth.getInstance().currentUser?.uid

                if (userId == null) {
                    Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
                    return
                }

                val userDoc = FirebaseFirestore.getInstance().collection("users").document(userId)
                userDoc.get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists() && snapshot.getBoolean("seeded") == true) {
                        Toast.makeText(this, "Seed data already added.", Toast.LENGTH_SHORT).show()
                    } else {
                        SeedData.seedTestData(userId)
                        userDoc.set(mapOf("seeded" to true), SetOptions.merge())
                        Toast.makeText(this, "Seed data added.", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to check seed status.", Toast.LENGTH_SHORT).show()
                }
            }



            binds.logoutButton.id -> {
                model.signOut()
                startActivity(Intent(this, SignInActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }

            binds.deleteAccountButton.id -> {
                showDeleteAccountConfirmation()
            }
        }
    }



    private fun setupClickListeners() {
        binds.manageGoalsButton.setOnClickListener(this)
        binds.achievementsButton.setOnClickListener(this)
        binds.logoutButton.setOnClickListener(this)
        binds.btnSeedData.setOnClickListener(this)
        binds.deleteAccountButton.setOnClickListener(this)
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
