package com.nutrition.express.ui.post.blog

import android.app.ActivityOptions
import android.app.SharedElementCallback
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.flexbox.FlexboxLayout
import com.nutrition.express.R
import com.nutrition.express.common.CommonViewHolder
import com.nutrition.express.databinding.ItemTrailBinding
import com.nutrition.express.imageviewer.ImageViewerActivity
import com.nutrition.express.model.api.Resource
import com.nutrition.express.model.data.AppData
import com.nutrition.express.model.api.bean.PhotoItem
import com.nutrition.express.model.api.bean.PostsItem
import com.nutrition.express.model.data.bean.PhotoPostsItem
import com.nutrition.express.ui.main.UserViewModel
import com.nutrition.express.ui.reblog.ReblogActivity
import com.nutrition.express.util.dp2Pixels
import com.nutrition.express.util.setTumblrAvatarUri
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

abstract class BasePostVH<T>(view: View) : CommonViewHolder<T>(view) {
    protected var isSimpleMode: Boolean = false
    protected val userViewModel: UserViewModel
    protected val blogViewModel: BlogViewModel

    private val contentViewCache = ArrayList<SimpleDraweeView>()
    private val trailViewCache = ArrayList<ItemTrailBinding>()
    private val atomicInteger = AtomicInteger(0)
    private val photos = ArrayList<String>()
    private val dividerWidth: Int
    private val screenWidth : Int

    init {
        val activity = itemView.context as AppCompatActivity
        val provider = ViewModelProvider(activity)
        userViewModel = provider.get()
        blogViewModel = provider.get()
        userViewModel.likeData.observe(activity, Observer {
            if (it is Resource.Success) {
                onLike(it.data)
            }
        })
        userViewModel.unLikeData.observe(activity, Observer {
            if (it is Resource.Success) {
                onUnLike(it.data)
            }
        })

        val dm = itemView.context.resources.displayMetrics
        screenWidth = dm.widthPixels
        dividerWidth = dp2Pixels(itemView.context, 4)
        isSimpleMode = AppData.modeData.value ?: false
    }

    abstract fun onLike(id: Long?)

    abstract fun onUnLike(id: Long?)

    protected fun setPhotoContent(postContent: FlexboxLayout, postsItem: PostsItem) {
        postContent.removeAllViews()
        photos.clear()
        for (item in postsItem.photos) {
            photos.add(item.original_size.url)
        }
        val size = photos.size
        createPhotoView(size)
        val layout = postsItem.photoset_layout
        if (layout!= null && layout.isDigitsOnly()) {
            var index = 0
            var count: Int
            var w: Int
            var h: Int
            var photoInfo: PhotoItem.PhotoInfo
            for (char in layout) {
                count = char - '0'
                if (index > size) return
                photoInfo = postsItem.photos[index].original_size
                w = calWidth(count)
                h = w * photoInfo.height / photoInfo.width
                for (j in 0 until count) {
                    if (index >= size) {
                        return
                    }
                    addSimpleDraweeView(postContent, contentViewCache[index], w, h)
                    setUri(contentViewCache[index],
                            postsItem.photos[index].original_size.url)
                    index++
                }
            }
        } else{
            var info: PhotoItem.PhotoInfo
            var w: Int
            var h: Int
            for (i in 0 until size) {
                info = postsItem.photos[i].original_size
                w = calWidth(1)
                h = if (info.width != 0) {
                    w * info.height / info.width
                } else {
                    w / 2
                }
                addSimpleDraweeView(postContent, contentViewCache[i], w, h)
                setUri(contentViewCache[i], info.url)
            }
        }
    }

    protected fun setVideoContent(postContent: FlexboxLayout, postsItem: PostsItem) {
        postContent.removeAllViews()
        createPhotoView(1)
        val draweeView = contentViewCache[0]
        val w = calWidth(1)
        val h: Int
        h = if (postsItem.thumbnail_width > 0) {
            w * postsItem.thumbnail_height / postsItem.thumbnail_width
        } else {
            w / 2
        }
        addSimpleDraweeView(postContent, draweeView, w, h)
        setUri(draweeView, postsItem.thumbnail_url)
    }

    protected fun setTrailContent(postTrail: LinearLayout, postsItem: PostsItem) {
        val trails = postsItem.trail
        if (trails != null && trails.size > 0) {
            postTrail.removeAllViews()
            createTrailView(trails.size)
            for (i in trails.indices) {
                val trailBinding = trailViewCache[i]
                setTumblrAvatarUri(trailBinding.trailAvatar, trails[i].blog.name, 128)
                trailBinding.trailName.text = trails[i].blog.name
                trailBinding.trailContent.text = fromHtlmCompat(trails[i].content_raw)
                trailBinding.root.tag = trails[i].blog.name
                postTrail.addView(trailBinding.root)
            }
            postTrail.visibility = View.VISIBLE
        } else {
            postTrail.visibility = View.GONE
        }
    }

    private fun fromHtlmCompat(html: String?): Spanned {
        val content = html ?: "..."
        return if (Build.VERSION.SDK_INT >= 24) {
            Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT) // for 24 api and more
        } else {
            Html.fromHtml(content) // or for older api
        }
    }

    private fun createTrailView(count: Int) {
        while (count > trailViewCache.size) {
            val binding = ItemTrailBinding.inflate(LayoutInflater.from(itemView.context))
            binding.root.setOnClickListener {
                openBlog(it.tag as String)
            }
            trailViewCache.add(binding)
        }
    }

    private fun calWidth(count: Int): Int {
        return (screenWidth - (count - 1) * dividerWidth) / count
    }

    private fun addSimpleDraweeView(postContent: FlexboxLayout, view: SimpleDraweeView, width: Int, height: Int) {
        var params = view.layoutParams
        if (params == null) {
            params = ViewGroup.LayoutParams(width, height)
        }
        params.width = width
        params.height = height
        view.layoutParams = params
        postContent.addView(view)
    }

    private fun setUri(view: SimpleDraweeView, url: String?) {
        val uri = if (url == null) Uri.EMPTY else Uri.parse(url)
        val controller = Fresco.newDraweeControllerBuilder().run {
            oldController = view.controller
            tapToRetryEnabled = true
            autoPlayAnimations = true
            setUri(uri)
            build()
        }
        view.controller = controller
    }

    private fun createPhotoView(count: Int) {
        while (count > contentViewCache.size) {
            contentViewCache.add(createSimpleDraweeView())
        }
    }

    private fun createSimpleDraweeView(): SimpleDraweeView {
        val view = SimpleDraweeView(itemView.context)
        val hierarchy = GenericDraweeHierarchyBuilder(itemView.context.resources).run {
            actualImageScaleType = ScalingUtils.ScaleType.CENTER_CROP
            setPlaceholderImage(R.color.loading_color)
            placeholderImageScaleType = ScalingUtils.ScaleType.FIT_CENTER
            setFailureImage(R.mipmap.ic_failed)
            failureImageScaleType = ScalingUtils.ScaleType.CENTER
            build()
        }
        view.hierarchy = hierarchy
        view.tag = atomicInteger.getAndIncrement()
        view.setOnClickListener { onPhotoClick(it) }
        return view
    }

    private fun onPhotoClick(view: View) {
        //click on photo
        val context = itemView.context
        if (view.tag is Int) {
            val tag = view.tag as Int
            val intent = Intent(context, ImageViewerActivity::class.java)
            intent.putExtra("selected_index", tag)
            intent.putStringArrayListExtra("image_urls", photos)
            val options = ActivityOptions.makeSceneTransitionAnimation(
                    context as AppCompatActivity, view, "name$tag")
            context.startActivity(intent, options.toBundle())
            setCallback(tag)
        }
    }

    private fun setCallback(index: Int) {
        val context = itemView.context
        AppData.photoIndex = index
        (context as AppCompatActivity).setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(names: List<String>, sharedElements: MutableMap<String, View>) {
                val returnIndex = AppData.photoIndex
                if (index != returnIndex && names.isNotEmpty()) {
                    sharedElements[names[0]] = contentViewCache[returnIndex]
                }
                context.setExitSharedElementCallback(null as SharedElementCallback?)
            }
        })
    }

    protected fun openBlog(blogName: String?) {
        blogName?.let {
            val intent = Intent(itemView.context, PostListActivity::class.java)
            intent.putExtra("blog_name", blogName)
            itemView.context.startActivity(intent)
        }
    }

    protected fun reblog(postsItem: PostsItem) {
        val intent = Intent(itemView.context, ReblogActivity::class.java)
        intent.putExtra("id", postsItem.id.toString())
        intent.putExtra("reblog_key", postsItem.reblog_key)
        intent.putExtra("type", postsItem.type)
        itemView.context.startActivity(intent)
    }

    protected fun showDeleteDialog(postsItem: PostsItem) {
        val builder = AlertDialog.Builder(itemView.context)
        builder.setPositiveButton(R.string.delete_positive) { dialog, which ->
            blogViewModel.deletePost(postsItem.blog_name, postsItem.id.toString())
        }
        builder.setNegativeButton(R.string.pic_cancel, null)
        builder.setTitle(R.string.delete_title)
        builder.show()
    }
}