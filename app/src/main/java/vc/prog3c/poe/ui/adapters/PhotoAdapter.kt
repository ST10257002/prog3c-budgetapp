package vc.prog3c.poe.ui.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import vc.prog3c.poe.R

class PhotoAdapter : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {
    private val photos = mutableListOf<Uri>()

    fun addPhoto(uri: Uri) {
        photos.add(uri)
        notifyItemInserted(photos.size - 1)
    }

    fun removePhoto(position: Int) {
        photos.removeAt(position)
        notifyItemRemoved(position)
    }

    fun getPhotos(): List<Uri> = photos.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(photos[position])
    }

    override fun getItemCount(): Int = photos.size

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val photoImageView: ImageView = itemView.findViewById(R.id.photoImageView)
        private val removeButton: ImageView = itemView.findViewById(R.id.removeButton)

        fun bind(uri: Uri) {
            photoImageView.setImageURI(uri)
            removeButton.setOnClickListener {
                // TODO: Implement remove functionality
            }
        }
    }
} 