package com.nutrition.express.main.v2;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.facebook.drawee.view.SimpleDraweeView;
import com.nutrition.express.R;
import com.nutrition.express.application.BaseActivity;
import com.nutrition.express.blogposts.PostListActivity;
import com.nutrition.express.common.CommonPagerAdapter;
import com.nutrition.express.download.DownloadManagerActivity;
import com.nutrition.express.downloading.DownloadingActivity;
import com.nutrition.express.following.FollowingActivity;
import com.nutrition.express.likes.LikesActivity;
import com.nutrition.express.main.DashboardFragment;
import com.nutrition.express.main.UserContract;
import com.nutrition.express.main.UserPresenter;
import com.nutrition.express.main.VideoDashboardFragment;
import com.nutrition.express.model.event.EventRefresh;
import com.nutrition.express.model.rest.bean.BlogInfoItem;
import com.nutrition.express.model.rest.bean.UserInfo;
import com.nutrition.express.search.SearchActivity;
import com.nutrition.express.settings.SettingsActivity;
import com.nutrition.express.util.FrescoUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class Main2Activity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, UserContract.View {
    private AppBarLayout appBarLayout;
    private DrawerLayout drawerLayout;
    private ViewPager viewPager;
    private MenuItem videoItem, photoItem;

    private VideoDashboardFragment videoDashboardFragment;
    private DashboardFragment photoFragment;

    private UserContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        appBarLayout = findViewById(R.id.appBarLayout);
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        videoItem = navigationView.getMenu().getItem(0);
        photoItem = navigationView.getMenu().getItem(1);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container), new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                return insets;
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appBarLayout), new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
//                v.setBackgroundResource(R.color.colorPrimaryDark);
                return insets;
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                ((AppBarLayout.LayoutParams) v.getLayoutParams()).topMargin = insets.getSystemWindowInsetTop();
                return insets.consumeSystemWindowInsets();
            }
        });

        List<Fragment> fragments = new ArrayList<>(2);
        List<String> titles = new ArrayList<>(2);

        videoDashboardFragment = new VideoDashboardFragment();
        photoFragment = new DashboardFragment();
        fragments.add(videoDashboardFragment);
        fragments.add(photoFragment);
        titles.add(getString(R.string.page_video));
        titles.add(getString(R.string.page_photo));

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new CommonPagerAdapter(getSupportFragmentManager(), fragments, titles));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    setTitle(R.string.page_video);
                    videoItem.setChecked(true);
                } else {
                    setTitle(R.string.page_photo);
                    photoItem.setChecked(true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        setTitle(R.string.page_video);
        videoItem.setChecked(true);

        presenter = new UserPresenter(this);
        presenter.getMyInfo();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onError(int code, String error) {

    }

    @Override
    public void showMyInfo(UserInfo info) {
        List<BlogInfoItem> list = info.getUser().getBlogs();
        final String[] names = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            names[i] = list.get(i).getName();
        }

        final SimpleDraweeView avatar = findViewById(R.id.user_avatar);
        final AppCompatSpinner spinner = findViewById(R.id.user_name);
        if (spinner == null) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FrescoUtils.setTumblrAvatarUri(avatar, names[position], 128);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main2Activity.this, PostListActivity.class);
                intent.putExtra("blog_name", (String) spinner.getSelectedItem());
                intent.putExtra("is_admin", true);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (viewPager.getCurrentItem() == 0 && !videoDashboardFragment.isAtTop()) {
                appBarLayout.setExpanded(true);
                videoDashboardFragment.scrollToTop();
            } else if (viewPager.getCurrentItem() == 1 && !photoFragment.isAtTop()) {
                appBarLayout.setExpanded(true);
                photoFragment.scrollToTop();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_downloading) {
            Intent intent = new Intent(this, DownloadingActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_downloaded) {
            Intent intent = new Intent(this, DownloadManagerActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_following) {
            Intent intent = new Intent(this, FollowingActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_like) {
            Intent intent = new Intent(this, LikesActivity.class);
            startActivity(intent);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDetach();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void refreshData(EventRefresh refreshEvent) {
        EventBus.getDefault().removeStickyEvent(refreshEvent);
        if (videoDashboardFragment != null) {
            videoDashboardFragment.refreshData();
        }
        if (photoFragment != null) {
            photoFragment.refreshData();
        }
    }

}
