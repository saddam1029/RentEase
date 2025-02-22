package com.example.rentease

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ImageAdapter(
    private val images: MutableList<Uri>,
    private val onImageRemoved: (Int) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.ivSelectedImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pics, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Glide.with(holder.itemView.context)
            .load(images[position])
            .into(holder.imageView)

        holder.imageView.setOnClickListener {
            showRemoveDialog(holder.itemView, position)
        }
    }

    private fun showRemoveDialog(view: View, position: Int) {
        AlertDialog.Builder(view.context)
            .setTitle("Remove Image")
            .setMessage("Do you want to remove this image?")
            .setPositiveButton("Yes") { _, _ ->
                onImageRemoved(position)
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun getItemCount(): Int = images.size
}