package com.nutrition.express.ui.post.tagged

import android.app.SearchManager
import android.content.Context
import android.media.AudioManager.STREAM_MUSIC
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.nutrition.express.R
import com.nutrition.express.application.BaseActivity
import com.nutrition.express.application.toast
import com.nutrition.express.common.CommonRVAdapter
import com.nutrition.express.common.MyExoPlayer
import com.nutrition.express.databinding.ActivityTaggedBinding
import com.nutrition.express.model.api.Resource
import com.nutrition.express.model.api.bean.PostsItem
import com.nutrition.express.model.data.bean.PhotoPostsItem
import com.nutrition.express.model.data.bean.VideoPostsItem
import com.nutrition.express.ui.post.blog.BlogViewModel
import com.nutrition.express.ui.post.blog.PhotoPostVH
import com.nutrition.express.ui.post.blog.VideoPostVH

class TaggedActivity : BaseActivity() {
    private lateinit var binding: ActivityTaggedBinding
    private lateinit var adapter: CommonRVAdapter
    private val taggedViewModel: TaggedViewModel by viewModels()
    private val blogViewModel: BlogViewModel by viewModels()
    private var featuredTimestamp: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaggedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = getAdapter()
        adapter.append(null, false)
        binding.recyclerView.adapter = adapter

        volumeControlStream = STREAM_MUSIC

        taggedViewModel.postData.observe(this, {
            when (it) {
                is Resource.Success -> {
                    val list = it.data
                    if (list != null && list.isNotEmpty()) {
                        featuredTimestamp = list[list.size - 1].postsItem.featured_timestamp
                    }
                    var hasNextPage = true
                    featuredTimestamp?.let { time -> hasNextPage = time > 0 }
                    adapter.append(list?.toTypedArray(), hasNextPage)
                }
                is Resource.Error -> adapter.showLoadingFailure(
                    getString(
                        R.string.load_failure_des,
                        it.code,
                        it.message
                    )
                )
                is Resource.Loading -> {
                }
            }
        })
        blogViewModel.deletePostData.observe(this, {
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
    }

    private fun getAdapter(): CommonRVAdapter {
        return CommonRVAdapter.adapter {
            addViewType(PhotoPostsItem::class, R.layout.item_post, ::PhotoPostVH)
            addViewType(VideoPostsItem::class, R.layout.item_video_post, ::VideoPostVH)
            loadListener = object : CommonRVAdapter.OnLoadListener {
                override fun retry() {
                    taggedViewModel.retryIfFailed()
                }

                override fun loadNextPage() {
                    taggedViewModel.getNextPage(featuredTimestamp)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val menuItem = menu.findItem(R.id.action_search)
        val searchView: SearchView? = menuItem?.actionView as SearchView
        searchView?.run {
            val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let {
                        taggedViewModel.setTag(query)
                        adapter.showReloading()
                    }
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(windowToken, 0)
                    searchView.clearFocus()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }
            })
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
        MyExoPlayer.stop()
    }

}
