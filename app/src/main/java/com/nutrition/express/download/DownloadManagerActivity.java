package com.nutrition.express.download;

import android.media.AudioManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import com.nutrition.express.R;
import com.nutrition.express.common.CommonPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huang on 2/17/17.
 */

public class DownloadManagerActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private VideoFragment videoFragment;
    private PhotoFragment photoFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_manager);

        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.download_toolbar_title);
        }
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsingToolbar);
        collapsingToolbarLayout.setTitleEnabled(false);


        viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    videoFragment.scrollToTop();
                } else if (tab.getPosition() == 1) {
                    photoFragment.scrollToTop();
                }
            }
        });

        setContentData();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setContentData() {
        List<Fragment> list = new ArrayList<>();
        List<String> titles = new ArrayList<>();

        videoFragment = new VideoFragment();
        photoFragment = new PhotoFragment();
        list.add(videoFragment);
        titles.add(getString(R.string.download_video_title));
        list.add(photoFragment);
        titles.add(getString(R.string.download_photo_title));

        CommonPagerAdapter pagerAdapter =
                new CommonPagerAdapter(getSupportFragmentManager(), list, titles);
        viewPager.setAdapter(pagerAdapter);
    }

    protected ActionMode startMultiChoice(ActionMode.Callback callback) {
        return startSupportActionMode(callback);
    }

}
