package vc.prog3c.poe.ui.views
/**
 * @reference Android Jetpack Navigation Component: https://developer.android.com/guide/navigation/navigation-getting-started
 * @reference WindowInsetsCompat for Edge-to-Edge Layouts: https://developer.android.com/develop/ui/views/layout/insets
 */

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import vc.prog3c.poe.R
import vc.prog3c.poe.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binds: ActivityMainBinding
    private lateinit var navController: NavController


    // --- Lifecycle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBindings()
        setupLayoutUi()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    // --- Internals


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }


    // --- UI


    private fun setupBindings() {
        binds = ActivityMainBinding.inflate(layoutInflater)
    }


    private fun setupLayoutUi() {
        setContentView(binds.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binds.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}