package com.nutrition.express.ui.main

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.nutrition.express.R
import com.nutrition.express.application.toast
import com.nutrition.express.common.CommonRVAdapter
import com.nutrition.express.common.MyExoPlayer
import com.nutrition.express.databinding.FragmentDashboardBinding
import com.nutrition.express.model.api.Resource
import com.nutrition.express.model.data.AppData
import com.nutrition.express.model.data.bean.PhotoPostsItem
import com.nutrition.express.model.data.bean.VideoPostsItem
import com.nutrition.express.model.api.bean.BlogPosts
import com.nutrition.express.model.api.bean.PostsItem
import com.nutrition.express.ui.post.blog.BlogViewModel
import com.nutrition.express.ui.post.blog.PhotoPostVH
import com.nutrition.express.ui.post.blog.VideoPostVH
import java.util.*


open class DashboardFragment : Fragment() {
    private var binding: FragmentDashboardBinding? = null
    private lateinit var adapter: CommonRVAdapter
    private var lastTimestamp = Long.MAX_VALUE
    private var offset = 0
    private var type: String = "video"

    private val userViewModel: UserViewModel by viewModels()
    private val blogViewModel: BlogViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val _binding = FragmentDashboardBinding.inflate(layoutInflater, container, false)
        binding = _binding
        adapter = CommonRVAdapter.Builder().run {
            addItemType(PhotoPostsItem::class.java, R.layout.item_post) { PhotoPostVH(it) }
            addItemType(VideoPostsItem::class.java, R.layout.item_video_post) { VideoPostVH(it)}
            loadListener = object : CommonRVAdapter.OnLoadListener {
                override fun retry() {
                    userViewModel.fetchDashboardNextPageData(offset)
                }

                override fun loadNextPage() {
                    userViewModel.fetchDashboardNextPageData(offset)
                }
            }
            build()
        }
        _binding.recyclerView.layoutManager = LinearLayoutManager(context)
        _binding.recyclerView.adapter = adapter
        _binding.refreshLayout.setOnRefreshListener {
            userViewModel.fetchDashboardData(type)
        }

        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViewModel()
    }

    override fun onPause() {
        super.onPause()
        MyExoPlayer.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun initViewModel() {
        blogViewModel.deletePostData.observe(viewLifecycleOwner, Observer {
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
                is Resource.Error -> Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                is Resource.Loading -> {}
            }
        })
        userViewModel.dashboardData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    binding?.refreshLayout?.isRefreshing = false
                    offset = 0
                    lastTimestamp = Long.MAX_VALUE
                    adapter.resetData(wrapPosts(it.data)?.toTypedArray(), true)
                }
                is Resource.Error -> {
                    binding?.refreshLayout?.isRefreshing = false
                    adapter.showLoadingFailure(it.message)
                }
                is Resource.Loading -> {}
            }
        })
        userViewModel.dashboardNextPageData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> adapter.append(wrapPosts(it.data)?.toTypedArray(), true)
                is Resource.Error -> adapter.showLoadingFailure(it.message)
                is Resource.Loading -> {}
            }
        })
        type = arguments?.getString("type") ?: type
        userViewModel.fetchDashboardData(type)
    }

    private fun wrapPosts(blogPosts: BlogPosts?): List<PhotoPostsItem>? {
        if (blogPosts == null) {
            adapter.showLoadingFinish()
            return null
        }
        val postsItems: List<PostsItem> = blogPosts.list
        var overload = 0
        for (item in postsItems) {
            AppData.addFollowingBlog(item.blog_name)
            if (!TextUtils.isEmpty(item.source_title)) {
                AppData.addReferenceBlog(item.source_title)
            }
            for (trailItem in item.trail) {
                if (!TextUtils.isEmpty(trailItem.blog.name)) {
                    AppData.addReferenceBlog(trailItem.blog.name)
                }
            }
            if (item.timestamp > lastTimestamp) {
                overload++
            }
        }
        lastTimestamp = postsItems[postsItems.size - 1].timestamp
        offset += postsItems.size + overload
        val list: MutableList<PhotoPostsItem> = ArrayList(postsItems.size)
        if (TextUtils.equals("video", type)) {
            for (index in overload until postsItems.size) {
                list.add(VideoPostsItem(postsItems[index]))
            }
        } else {
            for (index in overload until postsItems.size) {
                list.add(PhotoPostsItem(postsItems[index]))
            }
        }
        if (list.isEmpty()) {
            userViewModel.fetchDashboardNextPageData(offset)
        }
        return list
    }

    open fun scrollToTop(): Boolean {
        binding?.let {
            if (it.recyclerView.canScrollVertically(-1)) {
                it.recyclerView.scrollToPosition(0)
                return true
            }
        }
        return false

    }
}
