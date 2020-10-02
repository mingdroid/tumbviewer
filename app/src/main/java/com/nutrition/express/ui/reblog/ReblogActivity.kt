package com.nutrition.express.ui.reblog

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import androidx.activity.viewModels
import com.nutrition.express.R
import com.nutrition.express.application.BaseActivity
import com.nutrition.express.application.toast
import com.nutrition.express.databinding.ActivityReblogBinding
import com.nutrition.express.model.api.Resource
import com.nutrition.express.model.api.bean.UserInfoItem
import com.nutrition.express.model.data.AppData
import com.nutrition.express.ui.main.UserViewModel

class ReblogActivity : BaseActivity() {
    private lateinit var binding: ActivityReblogBinding
    private val reblogViewModel: ReblogViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    private var name: String? = null
    private lateinit var id: String
    private lateinit var key: String
    private lateinit var type: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReblogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.post.setOnClickListener {
            name?.let {
                reblogViewModel.reblog(it, id, key, type, binding.comment.text.toString())
            }
        }
        if (AppData.users == null) {
            userViewModel.userInfoData.observe(this, {
                when (it) {
                    is Resource.Success -> it.data?.user?.let(this::setNames)
                    is Resource.Error -> toast(it.message)
                    is Resource.Loading -> {
                    }
                }
            })
            userViewModel.fetchUserInfo()
        } else {
            setNames(AppData.users!!)
        }
        reblogViewModel.reblogResult.observe(this, {
            when (it) {
                is Resource.Success -> {
                    toast(R.string.reblog_success)
                    finish()
                }
                is Resource.Error -> {
                    binding.post.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    toast(it.message)
                }
                is Resource.Loading -> {
                    binding.post.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        })

        id = intent.getStringExtra("id") ?: ""
        key = intent.getStringExtra("reblog_key") ?: ""
        type = intent.getStringExtra("type") ?: ""
    }

    private fun setNames(user: UserInfoItem) {
        val names: MutableList<String> = ArrayList()
        user.blogs.mapTo(names) { it.name }
        if (names.isNotEmpty()) {
            name = names[0]
            binding.spinner.visibility = View.VISIBLE
            val adapter: SpinnerAdapter = ArrayAdapter(this, R.layout.item_text, R.id.text, names)
            binding.spinner.adapter = adapter
            binding.spinner.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    name = parent.getItemAtPosition(position) as String
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}
