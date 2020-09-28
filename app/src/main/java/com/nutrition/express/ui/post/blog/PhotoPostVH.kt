package com.nutrition.express.ui.post.blog

import android.text.format.DateUtils
import android.view.View
import com.nutrition.express.R
import com.nutrition.express.databinding.ItemPostBinding
import com.nutrition.express.model.api.bean.PostsItem
import com.nutrition.express.model.data.bean.PhotoPostsItem
import com.nutrition.express.util.setTumblrAvatarUri
import okhttp3.internal.toLongOrDefault

open class PhotoPostVH(view: View) : BasePostVH<PhotoPostsItem>(view) {
    protected val binding: ItemPostBinding = ItemPostBinding.bind(view)
    private var postsItem: PostsItem? = null

    init {
        binding.postHeader.setOnClickListener {
            postsItem?.let { openBlog(it.blog_name) }
        }
        binding.postSource.setOnClickListener {
            postsItem?.let { openBlog(it.blog_name) }
        }
        binding.postReblog.setOnClickListener {
            postsItem?.let { reblog(it) }
        }
        binding.postLike.setOnClickListener {
            postsItem?.let {
                if (binding.postLike.isSelected) {
                    userViewModel.unLike(it.id, it.reblog_key)
                } else {
                    userViewModel.like(it.id, it.reblog_key)
                }
            }
        }
        binding.postDelete.setOnClickListener {
            postsItem?.let { showDeleteDialog(it) }
        }
    }

    override fun onLike(id: Long?) {
        if (postsItem?.id == id) {
            postsItem?.isLiked = true
            binding.postLike.isSelected = true
        }
    }

    override fun onUnLike(id: Long?) {
        if (postsItem?.id == id) {
            postsItem?.isLiked = false
            binding.postLike.isSelected = false
        }
    }

    override fun bindView(item: PhotoPostsItem) {
        val postsItem = item.postsItem
        this.postsItem = postsItem
        setTumblrAvatarUri(binding.postAvatar, postsItem.blog_name, 128)
        binding.postName.text = postsItem.blog_name
        binding.postTime.text = DateUtils.getRelativeTimeSpanString(
            postsItem.getTimestamp() * 1000,
            System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS
        )
        if (postsItem.source_title.isNullOrEmpty()) {
            binding.postSource.visibility = View.GONE
        } else {
            binding.postSource.visibility = View.VISIBLE
            binding.postSource.text =
                itemView.context.getString(R.string.source_title, postsItem.source_title)
        }
        if (postsItem.duration.isNullOrEmpty()) {
            binding.noteCount.text =
                itemView.context.getString(R.string.note_count, postsItem.note_count)
        } else {
            binding.noteCount.text = itemView.context.getString(
                R.string.note_count_description,
                postsItem.note_count,
                DateUtils.formatElapsedTime(postsItem.duration.toLongOrDefault(0))
            )
        }
        if (postsItem.isCan_like) {
            binding.postLike.visibility = View.VISIBLE
            binding.postLike.isSelected = postsItem.isLiked
        } else {
            binding.postLike.visibility = View.GONE
        }
        if (postsItem.isCan_reblog) {
            binding.postReblog.visibility = View.VISIBLE
        } else {
            binding.postReblog.visibility = View.GONE
        }
        if (postsItem.isAdmin) {
            binding.postDelete.visibility = View.VISIBLE
        } else {
            binding.postDelete.visibility = View.GONE
        }
        when (postsItem.type) {
            "video" -> setVideoContent(binding.postContent, postsItem)
            "photo" -> setPhotoContent(binding.postContent, postsItem)
        }
        if (!isSimpleMode) {
            setTrailContent(binding.postTrail, postsItem)
        }
    }

}