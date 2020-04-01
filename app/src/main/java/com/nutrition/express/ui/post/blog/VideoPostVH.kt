package com.nutrition.express.ui.post.blog

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nutrition.express.R
import com.nutrition.express.databinding.ItemVideoPostBinding
import com.nutrition.express.model.data.bean.OnlineVideo
import com.nutrition.express.model.data.bean.VideoPostsItem
import com.nutrition.express.model.download.RxDownload
import com.nutrition.express.model.api.bean.PostsItem
import com.nutrition.express.util.canWrite2Storage
import com.nutrition.express.util.setTumblrAvatarUri
import okhttp3.internal.toLongOrDefault

class VideoPostVH(view: View) : BasePostVH(view) {
    private val binding : ItemVideoPostBinding = ItemVideoPostBinding.bind(view)
    private lateinit var onlineVideo: OnlineVideo
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
                } else{
                    userViewModel.like(it.id, it.reblog_key)
                }
            }
        }
        binding.postDelete.setOnClickListener {
            postsItem?.let { showDeleteDialog(it) }
        }
        binding.postVideo.setOnClickListener {
            postsItem?.let {
                if (!it.permalink_url.isNullOrEmpty()) {
                    //case "vine","youtube","instagram".
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.permalink_url))
                    try {
                        itemView.context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                    }
                }
            }
        }
        binding.postDownload.setOnClickListener {
            postsItem?.let {
                if (canWrite2Storage(itemView.context)) {
                    val status = RxDownload.getInstance().start(it.video_url, null)
                    if (status == RxDownload.PROCESSING) {
                        Toast.makeText(itemView.context, R.string.download_start, Toast.LENGTH_SHORT).show()
                    } else if (status == RxDownload.FILE_EXIST) {
                        Toast.makeText(itemView.context, R.string.video_exist, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(itemView.context, R.string.reblog_failure, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        binding.postDownload.setOnLongClickListener {
            postsItem?.let {
                val bundle = Bundle()
                bundle.putString("video_url", it.video_url)
                val bottomSheet = PostMoreDialog()
                bottomSheet.arguments = bundle
                bottomSheet.show((itemView.context as AppCompatActivity).supportFragmentManager, bottomSheet.tag)
            }
            return@setOnLongClickListener true
        }
    }

    override fun bindView(item: Any) {
        onlineVideo = (item as VideoPostsItem).onlineVideo
        if (item.postsItem.video_url.isNullOrEmpty()) {
            binding.postDownload.visibility = View.GONE
        } else{
            binding.postDownload.visibility = View.VISIBLE
        }
        val postsItem = item.postsItem
        this.postsItem = postsItem
        setTumblrAvatarUri(binding.postAvatar, postsItem.blog_name, 128)
        binding.postName.text = postsItem.blog_name
        binding.postTime.text = DateUtils.getRelativeTimeSpanString(postsItem.getTimestamp() * 1000,
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS)
        if (postsItem.source_title.isNullOrEmpty()) {
            binding.postSource.visibility = View.GONE
        } else {
            binding.postSource.visibility = View.VISIBLE
            binding.postSource.text = itemView.context.getString(R.string.source_title, postsItem.source_title)
        }
        if (postsItem.duration.isNullOrEmpty()) {
            binding.noteCount.text = itemView.context.getString(R.string.note_count, postsItem.note_count)
        } else {
            binding.noteCount.text = itemView.context.getString(R.string.note_count_description,
                    postsItem.note_count, DateUtils.formatElapsedTime(postsItem.duration.toLongOrDefault(0)))
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
            "video" -> setVideoContent(postsItem)
        }
        if (!isSimpleMode) {
            setTrailContent(binding.postTrail, postsItem)
        }
    }

    private fun setVideoContent(postsItem: PostsItem) {
        binding.postVideo.bindVideo(onlineVideo)
        if (TextUtils.equals("tumblr", postsItem.video_type)) {
            binding.postVideo.setPlayerClickable(true)
        } else {
            binding.postVideo.setPlayerClickable(false)
        }
    }

}