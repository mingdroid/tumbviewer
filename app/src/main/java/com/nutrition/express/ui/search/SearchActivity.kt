package com.nutrition.express.ui.search

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.nutrition.express.R
import com.nutrition.express.application.BaseActivity
import com.nutrition.express.common.CommonRVAdapter
import com.nutrition.express.common.CommonViewHolder
import com.nutrition.express.databinding.ActivitySearchBinding
import com.nutrition.express.databinding.ItemSearchReferBlogBinding
import com.nutrition.express.model.data.AppData
import com.nutrition.express.ui.post.blog.PostListActivity
import com.nutrition.express.util.setTumblrAvatarUri

class SearchActivity : BaseActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: CommonRVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null

        adapter = CommonRVAdapter.adapter {
            addViewType(String::class, R.layout.item_search_refer_blog) { BaseVH(it) }
        }
        binding.blogList.layoutManager = LinearLayoutManager(this)
        binding.blogList.adapter = adapter
        adapter.append(AppData.getReferenceBlog().toTypedArray(), false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val searchItem: MenuItem? = menu.findItem(R.id.action_search)
        val searchView: SearchView? = searchItem?.actionView as? SearchView
        searchView?.run {
            val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let { openBlogPosts(it) }
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

    private fun openBlogPosts(blogName: String) {
        val intent = Intent(this, PostListActivity::class.java)
        intent.putExtra("blog_name", blogName)
        startActivity(intent)
        adapter.notifyDataSetChanged()
    }

    private fun showDeleteDialog(position: Int) {
        AlertDialog.Builder(this).run {
            setMessage(R.string.download_delete_title)
            setPositiveButton(R.string.delete_positive) { _, _ ->
                adapter.notifyItemRemoved(position)
            }
            show()
        }
    }

    inner class BaseVH(view: View) : CommonViewHolder<String>(view) {
        private val binding: ItemSearchReferBlogBinding = ItemSearchReferBlogBinding.bind(view)
        private lateinit var name: String

        init {
            binding.root.setOnClickListener { openBlogPosts(name) }
            binding.root.setOnLongClickListener {
                showDeleteDialog(adapterPosition)
                return@setOnLongClickListener true
            }
        }

        override fun bindView(any: String) {
            binding.blogName.text = any
            setTumblrAvatarUri(binding.blogAvatar, any, 128)
        }
    }
}
