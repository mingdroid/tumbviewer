package com.nutrition.express.ui.main

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.ViewCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.navigation.NavigationView
import com.nutrition.express.R
import com.nutrition.express.application.BaseActivity
import com.nutrition.express.application.toast
import com.nutrition.express.common.CommonPagerAdapter
import com.nutrition.express.databinding.ActivityMain2Binding
import com.nutrition.express.model.api.Status
import com.nutrition.express.model.data.AppData
import com.nutrition.express.model.api.bean.UserInfo
import com.nutrition.express.ui.download.DownloadedActivity
import com.nutrition.express.ui.downloading.DownloadingActivity
import com.nutrition.express.ui.following.FollowingActivity
import com.nutrition.express.ui.likes.LikesActivity
import com.nutrition.express.ui.post.blog.PostListActivity
import com.nutrition.express.ui.post.blog.TestViewModel
import com.nutrition.express.ui.post.tagged.TaggedActivity
import com.nutrition.express.ui.search.SearchActivity
import com.nutrition.express.ui.settings.SettingsActivity
import com.nutrition.express.util.setTumblrAvatarUri
import kotlinx.android.synthetic.main.nav_header_main2.view.*

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMain2Binding
    private lateinit var videoItem: MenuItem
    private lateinit var photoItem: MenuItem

    private val userModel : UserViewModel by viewModels()

    private lateinit var videoFragment: DashboardFragment
    private lateinit var photoFragment: DashboardFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AppData.isLogin()) {
            toast("not login")
            finish()
            return
        }
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.content.toolbar)
        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.content.toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)
        videoItem = binding.navView.menu.getItem(0)
        photoItem = binding.navView.menu.getItem(1)


        ViewCompat.setOnApplyWindowInsetsListener(binding.content.content) { v, insets ->
            return@setOnApplyWindowInsetsListener insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.content.appBarLayout) { v, insets ->
            return@setOnApplyWindowInsetsListener insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.content.toolbar) { v, insets ->
            (v.layoutParams as AppBarLayout.LayoutParams).topMargin = insets.systemWindowInsetTop
            return@setOnApplyWindowInsetsListener insets.consumeSystemWindowInsets()
        }

        val bundle = Bundle()
        bundle.putString("type", "video")
        videoFragment = DashboardFragment()
        videoFragment.arguments = bundle
        val bundle1 = Bundle()
        bundle1.putString("type", "photo")
        photoFragment = DashboardFragment()
        photoFragment.arguments = bundle1
        val fragments = ArrayList<Fragment>(2)
        val titles = ArrayList<String>(2)
        fragments.add(videoFragment)
        fragments.add(photoFragment)
        titles.add(getString(R.string.page_video))
        titles.add(getString(R.string.page_photo))

        binding.content.viewPager.adapter = CommonPagerAdapter(supportFragmentManager, fragments, titles)
        binding.content.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        setTitle(R.string.page_video)
                        videoItem.isChecked = true
                    }
                    1 -> {
                        setTitle(R.string.page_photo)
                        photoItem.isChecked = true
                    }
                }
            }
        })
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            override fun onDrawerClosed(drawerView: View) {
            }

            override fun onDrawerOpened(drawerView: View) {
                userModel.retryIfFailed()
            }
        })

        setTitle(R.string.page_video)
        videoItem.isChecked = true

        volumeControlStream = AudioManager.STREAM_MUSIC

        initViewModel()
    }

    private fun initViewModel() {
        userModel.userInfoData.observe(this, Observer { resource ->
            when (resource.status) {
                Status.SUCCESS -> resource.data?.let { setUserInfo(it) }
                Status.LOADING -> {}
                Status.ERROR -> {}
            }
        })
        userModel.fetchUserInfo()
    }

    override fun onStart() {
        super.onStart()
        Log.d("MainActivity", "onStart:")
    }

    override fun onStop() {
        super.onStop()
        Log.d("MainActivity", "onStop: ${System.currentTimeMillis()}")
    }

    private fun setUserInfo(userInfo: UserInfo) {
        AppData.users = userInfo.user
        val blogs = userInfo.user.blogs
        val names: Array<String> = Array(blogs.size, init = {i ->  blogs[i].name})

        val adapter: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.navView.user_name.adapter = adapter
        binding.navView.user_name.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                setTumblrAvatarUri(binding.navView.user_avatar, names[position], 128)
            }
        }
        setTumblrAvatarUri(binding.navView.user_avatar, names[0], 128)
        binding.navView.user_avatar.setOnClickListener {
            val intent = Intent(this@MainActivity, PostListActivity::class.java)
            intent.putExtra("blog_name", binding.navView.user_name.selectedItem as String)
            intent.putExtra("is_admin", true)
            startActivity(intent)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_downloading ->
                startActivity(Intent(this, DownloadingActivity::class.java))
            R.id.nav_settings ->
                startActivity(Intent(this, SettingsActivity::class.java))
            R.id.nav_downloaded ->
                startActivity(Intent(this, DownloadedActivity::class.java))
            R.id.nav_following ->
                startActivity(Intent(this, FollowingActivity::class.java))
            R.id.nav_like ->
                startActivity(Intent(this, LikesActivity::class.java))
            R.id.nav_tags_search ->
                startActivity(Intent(this, TaggedActivity::class.java))
        }
        binding.drawerLayout.closeDrawer(binding.navView)
        return true
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(binding.navView)) {
            binding.drawerLayout.closeDrawer(binding.navView)
        } else if (binding.content.viewPager.currentItem == 0 && videoFragment.scrollToTop()) {
            binding.content.appBarLayout.setExpanded(true)
        } else if (binding.content.viewPager.currentItem == 1 && photoFragment.scrollToTop()) {
            binding.content.appBarLayout.setExpanded(true)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main2, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_search) {
            startActivity(Intent(this, SearchActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}