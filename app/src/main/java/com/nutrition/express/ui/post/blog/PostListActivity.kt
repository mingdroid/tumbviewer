package com.nutrition.express.ui.post.blog

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.nutrition.express.R
import com.nutrition.express.application.BaseActivity
import com.nutrition.express.application.toast
import com.nutrition.express.common.CommonRVAdapter
import com.nutrition.express.databinding.ActivityBlogPostsBinding
import com.nutrition.express.model.api.Resource
import com.nutrition.express.model.api.bean.BlogPosts
import com.nutrition.express.model.api.bean.PostsItem
import com.nutrition.express.model.data.bean.PhotoPostsItem
import com.nutrition.express.model.data.bean.VideoPostsItem
import com.nutrition.express.ui.likes.LikesActivity
import com.nutrition.express.ui.main.UserViewModel
import com.nutrition.express.util.getInt
import com.nutrition.express.util.putInt
import java.util.*

class PostListActivity : BaseActivity() {
    private lateinit var binding: ActivityBlogPostsBinding
    private lateinit var blogName: String
    private var followItem: MenuItem? = null
    private var followed = false

    private val TYPES = arrayOf("", "video", "photo")
    private val FILTER_TYPE = "filter_type"
    private var filter: Int = 0
    private var offset: Int = 0
    private var reset: Boolean = false

    private val userViewModel: UserViewModel by viewModels()
    private val blogViewModel: BlogViewModel by viewModels()
    private lateinit var adapter: CommonRVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlogPostsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        blogName = intent.getStringExtra("blog_name") ?: ""

        supportActionBar?.title = blogName

        filter = getInt(FILTER_TYPE)
        if (filter >= TYPES.size) {
            filter = 0
        }

        adapter = CommonRVAdapter.adapter {
            addViewType(PhotoPostsItem::class, R.layout.item_post) { PhotoPostVH(it) }
            addViewType(VideoPostsItem::class, R.layout.item_video_post) { VideoPostVH(it) }
            loadListener = object : CommonRVAdapter.OnLoadListener {
                override fun retry() {
                    blogViewModel.fetchBlogPosts(blogName, TYPES[filter], offset)
                }

                override fun loadNextPage() {
                    blogViewModel.fetchBlogPosts(blogName, TYPES[filter], offset)
                }
            }
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        initViewModel()

        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    private fun initViewModel() {
        blogViewModel.blogPostsData.observe(this, Observer {
            when (it) {
                is Resource.Success -> {
                    if (it.data == null) {
                        adapter.showLoadingFinish()
                    } else {
                        showPosts(it.data)
                    }
                }
                is Resource.Error -> adapter.showLoadingFailure(it.message)
                is Resource.Loading -> {
                }
            }
        })
        blogViewModel.deletePostData.observe(this, Observer {
            when (it) {
                is Resource.Success -> {
                    val list = adapter.getData()
                    for (index in list.indices) {
                        val item = list[index]
                        if (item is PostsItem) {
                            if (it.data == item.id.toString()) {
                                adapter.remove(index)
                            }
                        }
                    }
                }
                is Resource.Error -> toast(it.message)
                is Resource.Loading -> {
                }
            }
        })
        blogViewModel.fetchBlogPosts(blogName, TYPES[filter], offset)
        userViewModel.followData.observe(this, Observer {
            when (it) {
                is Resource.Success -> onFollowed()
                is Resource.Error -> toast(it.message)
                is Resource.Loading -> {
                }
            }
        })
        userViewModel.unFollowData.observe(this, Observer {
            when (it) {
                is Resource.Success -> onUnfollowed()
                is Resource.Error -> toast(it.message)
                is Resource.Loading -> {
                }
            }
        })
    }

    private fun reloading(which: Int) {
        adapter.showReloading()
        reset = true
        offset = 0
        filter = which
        blogViewModel.fetchBlogPosts(blogName, TYPES[filter], offset)
        putInt(FILTER_TYPE, which)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        var isAdmin = false
        if (intent != null) {
            isAdmin = intent.getBooleanExtra("is_admin", false)
        }
        menuInflater.inflate(R.menu.menu_blog, menu)
        followItem = menu.findItem(R.id.blog_follow)
        if (isAdmin) {
            followItem?.isVisible = false
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.blog_follow -> {
                if (followed) {
                    userViewModel.unFollow(blogName)
                } else {
                    userViewModel.follow(blogName)
                }
                true
            }
            R.id.post_filter -> {
                showFilterDialog()
                true
            }
            R.id.blog_likes -> {
                val intent = Intent(this, LikesActivity::class.java)
                intent.putExtra("blog_name", blogName)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showFilterDialog() {
        AlertDialog.Builder(this).run {
            setSingleChoiceItems(R.array.post_filter_type, filter) { dialog, which ->
                dialog.dismiss()
                reloading(which)
            }
            create()
            show()
        }
    }

    private fun onFollowed() {
        followItem?.let {
            it.title = getString(R.string.blog_unfollow)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            followed = true
        }
    }

    private fun onUnfollowed() {
        followItem?.let {
            it.title = getString(R.string.blog_follow)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            followed = false
        }
    }

    private fun showPosts(blogPosts: BlogPosts) {
        offset += blogPosts.list.size
        var hasNext = true
        if (blogPosts.list.size < 20 || offset >= blogPosts.count) {
            hasNext = false
        }
        val isAdmin = blogPosts.blogInfo.isAdmin
        if (isAdmin) {
            followItem?.isVisible = false
        } else if (blogPosts.blogInfo.isFollowed) {
            onFollowed()
        } else {
            onUnfollowed()
        }
        val postsItems: MutableList<PhotoPostsItem> = ArrayList(blogPosts.list.size)
        if (filter == 0) {
            //trim to only show videos and photos
            for (item in blogPosts.list) {
                item.isAdmin = isAdmin
                if (TextUtils.equals(item.type, "video")) {
                    postsItems.add(VideoPostsItem(item))
                } else if (TextUtils.equals(item.type, "photo")) {
                    postsItems.add(PhotoPostsItem(item))
                }
            }
        } else if (filter == 1) {
            for (item in blogPosts.list) {
                item.isAdmin = isAdmin
                postsItems.add(VideoPostsItem(item))
            }
        } else if (filter == 2) {
            for (item in blogPosts.list) {
                item.isAdmin = isAdmin
                postsItems.add(PhotoPostsItem(item))
            }
        }
        if (reset) {
            adapter.resetData(postsItems.toTypedArray(), hasNext)
            reset = false
        } else {
            adapter.append(postsItems.toTypedArray(), hasNext)
        }
    }

    private fun wrap() {

    }

}
