package com.example.clickit

import android.content.Context
import android.text.format.DateUtils
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.clickit.models.Posts
import android.view.LayoutInflater
import com.bumptech.glide.Glide

import kotlinx.android.synthetic.main.item_post.view.*
import java.math.BigInteger
import java.security.MessageDigest

class PostsAdapter (val context: Context, val post: List<Posts>) :
    RecyclerView.Adapter<PostsAdapter.ViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = post.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(post[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(posts: Posts) {
            val username = posts.user?.username as String
            itemView.tvUsername.text = posts.user?.username // due to new updated kotlin deprecated feature had to do view binding functionality -- reference text view content and then modify the property in kotlin files
            itemView.tvDescription.text = posts.description // want access to the id so that we can work with the xml file properties
            Glide.with(context).load(posts.imageURL).into(itemView.ivPost)
            Glide.with(context).load(getProfileImageUrl(username)).into(itemView.ivProfileImage)
            itemView.tvRelativeTime.text = DateUtils.getRelativeTimeSpanString(posts.creationTimeMS)

        }

        private fun getProfileImageUrl(username: String): String {
            val digest = MessageDigest.getInstance("MD5");
            val hash = digest.digest(username.toByteArray());
            val bigInt = BigInteger(hash)
            val hex = bigInt.abs().toString(16) // converting it to hex and then using it below in the url
            return "https://www.gravatar.com/avatar/$hex?d=identicon"; // using the hex in the url to capture avatar from gravatar


        }
    }

}