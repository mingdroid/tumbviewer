package com.nutrition.express.main;

import android.media.AudioManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.nutrition.express.R;
import com.nutrition.express.application.BaseActivity;
import com.nutrition.express.common.CommonPagerAdapter;
import com.nutrition.express.model.event.EventRefresh;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BaseActivity {
    private ViewPager viewPager;

    private DashboardFragment photoFragment;
    private VideoDashboardFragment videoFragment;
    private SearchFragment searchFragment;
    private UserFragment userFragment;

    private List<Fragment> list = new ArrayList<>();
    private List<String> titles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(5);
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
                if (tab.getPosition() == 0 && videoFragment != null) {
                    videoFragment.scrollToTop();
                } else if (tab.getPosition() == 1 && photoFragment != null) {
                    photoFragment.scrollToTop();
                }
            }
        });

        titles.add(getString(R.string.page_video));
        titles.add(getString(R.string.page_photo));
        titles.add(getString(R.string.page_search));
        titles.add(getString(R.string.page_user));
        videoFragment = new VideoDashboardFragment();
        photoFragment = new DashboardFragment();
        searchFragment = new SearchFragment();
        userFragment = new UserFragment();
        list.clear();
        list.add(videoFragment);
        list.add(photoFragment);
        list.add(searchFragment);
        list.add(userFragment);
        viewPager.setAdapter(new CommonPagerAdapter(getSupportFragmentManager(), list, titles));

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void refreshData(EventRefresh refreshEvent) {
        EventBus.getDefault().removeStickyEvent(refreshEvent);
        videoFragment.refreshData();
        photoFragment.refreshData();
        searchFragment.refreshData();
        userFragment.refreshData();
        viewPager.setCurrentItem(0);
    }

}
