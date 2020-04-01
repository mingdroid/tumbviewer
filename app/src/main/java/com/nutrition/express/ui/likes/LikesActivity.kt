package com.nutrition.express.ui.likes

import android.media.AudioManager
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.nutrition.express.R
import com.nutrition.express.application.BaseActivity
import com.nutrition.express.common.CommonRVAdapter
import com.nutrition.express.databinding.ActivityLikesBinding
import com.nutrition.express.model.api.Status
import com.nutrition.express.model.data.bean.PhotoPostsItem
import com.nutrition.express.model.data.bean.VideoPostsItem
import com.nutrition.express.model.api.bean.BlogLikes
import com.nutrition.express.model.api.bean.PostsItem
import com.nutrition.express.ui.post.blog.PhotoPostVH
import com.nutrition.express.ui.post.blog.VideoPostVH
import java.util.*

class LikesActivity : BaseActivity() {
    private lateinit var binding: ActivityLikesBinding
    private var blogName: String? = null
    private val likesViewModel: LikesViewModel by viewModels()
    private lateinit var adapter: CommonRVAdapter
    private var before = 0L
    private var total = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLikesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        blogName = intent.getStringExtra("blog_name")

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (blogName == null) {
            supportActionBar?.title = getString(R.string.page_user_like)
        } else {
            supportActionBar?.title = getString(R.string.likes_title, blogName)
        }
        binding.collapsingToolbar.isTitleEnabled = true

        volumeControlStream = AudioManager.STREAM_MUSIC

        initRecyclerView()
        initViewModel()
    }

    private fun initRecyclerView() {
        adapter = CommonRVAdapter.Builder().run {
            addItemType(PhotoPostsItem::class.java, R.layout.item_post) { PhotoPostVH(it) }
            addItemType(VideoPostsItem::class.java, R.layout.item_video_post) { VideoPostVH(it) }
            loadListener = object : CommonRVAdapter.OnLoadListener {
                override fun retry() {
                    likesViewModel.fetchNextPage(before)
                }

                override fun loadNextPage() {
                    likesViewModel.fetchNextPage(before)
                }
            }
            build()
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun initViewModel() {
        likesViewModel.likesPostsData.observe(this, Observer {
            when (it.status) {
                Status.LOADING -> {}
                Status.ERROR -> {
                    adapter.showLoadingFailure(it.message)
                }
                Status.SUCCESS -> {
                    if (it.data != null) {
                        showPosts(it.data)
                    } else {
                        adapter.showLoadingFinish()
                    }
                }
            }
        })
        likesViewModel.fetchLikesPosts(blogName)
    }

    private fun showPosts(likes: BlogLikes) {
        val data: List<PostsItem> = likes.list
        total += data.size
        if (data.isNotEmpty()) {
            before = data[data.size - 1].timestamp
        }
        var hasNext = true
        if (total >= likes.count || data.isEmpty()) {
            hasNext = false
        }
        //trim to only show videos and photos
        val postsItems: MutableList<PhotoPostsItem> = ArrayList(data.size)
        for (item in data) {
            if (TextUtils.equals(item.type, "video")) {
                postsItems.add(VideoPostsItem(item))
            } else if (TextUtils.equals(item.type, "photo")) {
                postsItems.add(PhotoPostsItem(item))
            }
        }
        if (hasNext && postsItems.size < 1) {
            likesViewModel.fetchNextPage(before)
        } else {
            adapter.append(postsItems.toTypedArray(), hasNext)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}
