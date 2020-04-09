package com.nutrition.express.ui.following

import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.nutrition.express.R
import com.nutrition.express.application.BaseActivity
import com.nutrition.express.common.CommonRVAdapter
import com.nutrition.express.common.CommonViewHolder
import com.nutrition.express.databinding.ActivityFollowingBinding
import com.nutrition.express.databinding.ItemFollowingBlogBinding
import com.nutrition.express.model.api.Resource
import com.nutrition.express.model.api.bean.FollowingBlog
import com.nutrition.express.ui.post.blog.PostListActivity
import com.nutrition.express.util.setTumblrAvatarUri

class FollowingActivity : BaseActivity() {
    private val followingViewModel: FollowingViewModel by viewModels()
    private lateinit var binding: ActivityFollowingBinding
    private lateinit var adapter: CommonRVAdapter
    private var offset = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFollowingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = CommonRVAdapter.Builder().run {
            addItemType(FollowingBlog.Blog::class.java, R.layout.item_following_blog) { BlogVH(it) }
            loadListener = object : CommonRVAdapter.OnLoadListener {
                override fun retry() {
                    followingViewModel.getFollowingList(offset)
                }

                override fun loadNextPage() {
                    followingViewModel.getFollowingList(offset)
                }
            }
            build()
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        followingViewModel.followingData.observe(this, Observer {
            when (it) {
                is Resource.Success -> {
                    if (it.data == null) {
                        adapter.showLoadingFinish()
                    } else {
                        offset += it.data.blogs.size
                        adapter.append(it.data.blogs.toTypedArray(), true)
                    }
                }
                is Resource.Error -> adapter.showLoadingFailure(it.message)
                is Resource.Loading -> {}
            }
        })
        followingViewModel.getFollowingList(offset)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    class BlogVH(view: View) : CommonViewHolder(view) {
        private val binding =  ItemFollowingBlogBinding.bind(view)
        private lateinit var blog: FollowingBlog.Blog

        init {
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, PostListActivity::class.java)
                intent.putExtra("blog_name", blog.name)
                itemView.context.startActivity(intent)
            }
        }

        override fun bindView(blog: Any) {
            this.blog = blog as FollowingBlog.Blog
            binding.blogName.text = blog.name
            binding.blogTitle.text = blog.title
            setTumblrAvatarUri(binding.blogAvatar, blog.name, 128)
            binding.blogLastUpdate.text = itemView.resources.getString(R.string.update_des,
                    DateUtils.getRelativeTimeSpanString(blog.updated * 1000,
                            System.currentTimeMillis(),
                            DateUtils.SECOND_IN_MILLIS))
        }
    }

}
