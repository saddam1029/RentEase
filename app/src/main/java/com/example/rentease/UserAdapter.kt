package com.example.rentease

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rentease.databinding.ItemPostBinding

class UserAdapter(private val postList: List<Post>) : RecyclerView.Adapter<UserAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int = postList.size

    inner class PostViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(post: Post) {
            binding.tvArea.text = post.propertyArea
            binding.tvPrice.text = post.price
            binding.tvLocation.text = post.location
            binding.tvRoom.text = "${post.rooms} Rooms"
            binding.tvType.text = post.propertyType

            post.imageUrls?.firstOrNull()?.let { firstImageUrl ->
                Glide.with(itemView.context)
                    .load(firstImageUrl)
                    .placeholder(R.drawable.blank)
                    .error(R.drawable.blank)
                    .into(binding.ivPics)
            } ?: run {
                binding.ivPics.setImageResource(R.drawable.blank)
            }

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, PostDetailActivity::class.java)
                intent.putExtra("description", post.description)
                intent.putExtra("location", post.location)
                intent.putExtra("price", post.price)
                intent.putExtra("propertyArea", post.propertyArea)
                intent.putExtra("propertyType", post.propertyType)
                intent.putExtra("rooms", post.rooms)
                intent.putExtra("editCode", 1)
                intent.putExtra("postId", post.postId)
                itemView.context.startActivity(intent)
            }
        }
    }
}