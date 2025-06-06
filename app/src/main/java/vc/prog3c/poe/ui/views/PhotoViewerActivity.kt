package vc.prog3c.poe.ui.views

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import vc.prog3c.poe.R
import vc.prog3c.poe.databinding.ActivityPhotoViewerBinding

class PhotoViewerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPhotoViewerBinding
    private var isSystemUiVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get photo URI from intent
        val photoUri = intent.getStringExtra("photo_uri")
        if (photoUri == null) {
            finish()
            return
        }

        setupToolbar()
        setupPhotoView(photoUri)
        setupSystemUiVisibility()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Photo"
    }

    private fun setupPhotoView(photoUri: String) {
        binding.photoView.setOnClickListener {
            toggleSystemUiVisibility()
        }

        // Load photo using Glide
        Glide.with(this)
            .load(photoUri)
            .into(binding.photoView)
    }

    private fun setupSystemUiVisibility() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun toggleSystemUiVisibility() {
        isSystemUiVisible = !isSystemUiVisible
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            if (isSystemUiVisible) {
                controller.show(WindowInsetsCompat.Type.systemBars())
                binding.toolbar.visibility = View.VISIBLE
            } else {
                controller.hide(WindowInsetsCompat.Type.systemBars())
                binding.toolbar.visibility = View.GONE
            }
        }
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