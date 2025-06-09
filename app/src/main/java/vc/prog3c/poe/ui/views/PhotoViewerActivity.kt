package vc.prog3c.poe.ui.views

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import vc.prog3c.poe.databinding.ActivityPhotoViewerBinding
/**
 * @reference Android Photo Picker API (13+): https://developer.android.com/media/picker
 * @reference Android Permissions for External Storage: https://developer.android.com/training/data-storage/shared/media#permissions
 */

class PhotoViewerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPhotoViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Photo"

        val photoUri = intent.getStringExtra(EXTRA_PHOTO_URI)
        photoUri?.let { uri ->
            Glide.with(this)
                .load(uri)
                .into(binding.ivImage)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_PHOTO_URI = "extra_photo_uri"
    }
} 