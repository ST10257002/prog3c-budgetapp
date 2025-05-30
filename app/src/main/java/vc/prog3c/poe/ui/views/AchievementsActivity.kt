package vc.prog3c.poe.ui.views

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import vc.prog3c.poe.data.models.Achievement
import vc.prog3c.poe.data.models.AchievementCategory
import vc.prog3c.poe.data.models.BoosterBucks
import vc.prog3c.poe.databinding.ActivityAchievementsBinding
import vc.prog3c.poe.ui.adapters.AchievementAdapter
import vc.prog3c.poe.ui.viewmodels.AchievementViewModel
import java.text.NumberFormat
import java.util.Locale

class AchievementsActivity : AppCompatActivity() {
    private lateinit var vBinds: ActivityAchievementsBinding
    private lateinit var viewModel: AchievementViewModel
    private lateinit var adapter: AchievementAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vBinds = ActivityAchievementsBinding.inflate(layoutInflater)
        setContentView(vBinds.root)
        ViewCompat.setOnApplyWindowInsetsListener(vBinds.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(this)[AchievementViewModel::class.java]

        setupToolbar()
        setupRecyclerView()
        setupTabLayout()
        setupBoosterBucksCard()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(vBinds.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Achievements"
    }

    private fun setupRecyclerView() {
        adapter = AchievementAdapter(emptyList()) { achievement ->
            showAchievementDetails(achievement)
        }
        vBinds.achievementsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AchievementsActivity)
            adapter = this@AchievementsActivity.adapter
        }
    }

    private fun setupTabLayout() {
        vBinds.achievementTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val category = when (tab?.position) {
                    0 -> null // All
                    1 -> AchievementCategory.USER_MILESTONES
                    2 -> AchievementCategory.CONSISTENCY_HABITS
                    3 -> AchievementCategory.SAVINGS_ACHIEVEMENTS
                    4 -> AchievementCategory.BUDGET_MANAGEMENT
                    5 -> AchievementCategory.FINANCIAL_INSIGHT
                    6 -> AchievementCategory.LEARNING_GROWTH
                    else -> null
                }
                adapter.filterByCategory(category)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupBoosterBucksCard() {
        vBinds.redeemButton.setOnClickListener {
            showRedeemDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.achievements.observe(this) { achievements ->
            adapter.updateAchievements(achievements)
        }

        viewModel.boosterBucks.observe(this) { boosterBucks ->
            updateBoosterBucksDisplay(boosterBucks)
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Snackbar.make(vBinds.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun updateBoosterBucksDisplay(boosterBucks: BoosterBucks) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        vBinds.boosterBucksBalance.text = boosterBucks.availableBalance.toString()
        vBinds.boosterBucksValue.text = formatter.format(boosterBucks.availableBalance * BoosterBucks.CONVERSION_RATE)
    }

    private fun showAchievementDetails(achievement: Achievement) {
        val message = if (achievement.isCompleted) {
            "Completed on ${achievement.completedAt?.toString() ?: "Unknown date"}"
        } else {
            "Progress: ${achievement.progress}/${achievement.requiredProgress}"
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(achievement.title)
            .setMessage("$message\n\n${achievement.description}")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showRedeemDialog() {
        val boosterBucks = viewModel.boosterBucks.value ?: return
        if (boosterBucks.availableBalance < BoosterBucks.MIN_REDEMPTION) {
            Snackbar.make(
                vBinds.root,
                "You need at least ${BoosterBucks.MIN_REDEMPTION} Booster Bucks to redeem",
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        val redeemAmount = boosterBucks.availableBalance * BoosterBucks.CONVERSION_RATE

        MaterialAlertDialogBuilder(this)
            .setTitle("Redeem Booster Bucks")
            .setMessage("You can redeem ${boosterBucks.availableBalance} Booster Bucks for ${formatter.format(redeemAmount)}")
            .setPositiveButton("Redeem") { _, _ ->
                viewModel.redeemBoosterBucks()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
} 