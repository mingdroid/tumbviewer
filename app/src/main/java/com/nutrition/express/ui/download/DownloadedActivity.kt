package com.nutrition.express.ui.download

import android.media.AudioManager
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.nutrition.express.R
import com.nutrition.express.application.BaseActivity
import com.nutrition.express.common.CommonPagerAdapter
import com.nutrition.express.databinding.ActivityDownloadManagerBinding
import java.util.*

class DownloadedActivity : BaseActivity() {
    private lateinit var binding: ActivityDownloadManagerBinding
    private lateinit var videoFragment: VideoFragment
    private lateinit var photoFragment: PhotoFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.download_toolbar_title)
        binding.collapsingToolbar.isTitleEnabled = true
        binding.tabLayout.setupWithViewPager(binding.viewPager)
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0) {
                    videoFragment.scrollToTop()
                } else if (tab.position == 1) {
                    photoFragment.scrollToTop()
                }
            }
        })

        val list: MutableList<Fragment> = ArrayList()
        val titles: MutableList<String> = ArrayList()
        videoFragment = VideoFragment()
        photoFragment = PhotoFragment()
        list.add(videoFragment)
        titles.add(getString(R.string.download_video_title))
        list.add(photoFragment)
        titles.add(getString(R.string.download_photo_title))
        val pagerAdapter = CommonPagerAdapter(supportFragmentManager, list, titles)
        binding.viewPager.adapter = pagerAdapter

        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}
