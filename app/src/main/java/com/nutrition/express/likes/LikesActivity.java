package com.nutrition.express.likes;

import android.media.AudioManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.nutrition.express.R;
import com.nutrition.express.application.BaseActivity;

/**
 * Created by huang on 11/8/16.
 */

public class LikesActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_likes);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        Bundle bundle = getIntent().getBundleExtra("bundle");
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (bundle == null) {
                actionBar.setTitle(getString(R.string.page_user_like));
            } else {
                actionBar.setTitle(getString(R.string.likes_title, bundle.getString("blog_name")));
            }
        }
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsingToolbar);
        collapsingToolbarLayout.setTitleEnabled(false);

        LikesFragment likesFragment = new LikesFragment();
        likesFragment.setArguments(bundle);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, likesFragment);
        ft.commit();

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
}
