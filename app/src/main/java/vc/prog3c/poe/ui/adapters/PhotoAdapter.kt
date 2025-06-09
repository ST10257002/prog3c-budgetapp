package vc.prog3c.poe.ui.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import vc.prog3c.poe.databinding.ItemPhotoBinding
/**
 * @reference Glide - Image Loading: https://github.com/bumptech/glide
 * @reference Android RecyclerView Adapter with ListAdapter: https://developer.android.com/reference/androidx/recyclerview/widget/ListAdapter
 */

class PhotoAdapter(
    private val onPhotoClick: (Uri) -> Unit,
    private val onRemoveClick: ((Uri) -> Unit)?
) : ListAdapter<Uri, PhotoAdapter.PhotoViewHolder>(PhotoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPhotoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updatePhotos(photos: List<Uri>) {
        submitList(photos)
    }

    inner class PhotoViewHolder(
        private val binding: ItemPhotoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(uri: Uri) {
            Glide.with(binding.root)
                .load(uri)
                .centerCrop()
                .into(binding.ivImage)

            binding.root.setOnClickListener { onPhotoClick(uri) }
            binding.removeButton.setOnClickListener { onRemoveClick?.invoke(uri) }
        }
    }

    private class PhotoDiffCallback : DiffUtil.ItemCallback<Uri>() {
        override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }
    }
} 