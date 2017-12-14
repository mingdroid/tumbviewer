package com.nutrition.express.blogposts;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.nutrition.express.R;
import com.nutrition.express.application.BaseActivity;
import com.nutrition.express.likes.LikesActivity;
import com.nutrition.express.useraction.FollowBlogContract;
import com.nutrition.express.useraction.FollowBlogPresenter;

/**
 * Created by huang on 5/16/16.
 */
public class PostListActivity extends BaseActivity implements FollowBlogContract.View {
    private MenuItem followItem;
    private FollowBlogPresenter followBlogPresenter;
    private boolean followed = false;
    private String blogName;
    private PostContract.Presenter presenter;
    private PostListFragment postListFragment;
    private Bundle blogBundle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_posts);

        Intent intent = getIntent();
        blogName = intent.getStringExtra("blog_name");
        blogBundle = new Bundle();
        blogBundle.putString("blog_name", blogName);

        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(blogName);
        }
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsingToolbar);
        collapsingToolbarLayout.setTitleEnabled(false);

        postListFragment = new PostListFragment();
        postListFragment.setArguments(blogBundle);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, postListFragment);
        ft.commit();

        followBlogPresenter = new FollowBlogPresenter(this);
        presenter = new PostPresenter(null);
        postListFragment.setPresenter(presenter);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean isAdmin = false;
        Intent intent = getIntent();
        if (intent != null) {
            isAdmin = intent.getBooleanExtra("is_admin", false);
        }
        getMenuInflater().inflate(R.menu.menu_blog, menu);
        followItem = menu.findItem(R.id.blog_follow);
        if (isAdmin) {
            followItem.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.blog_follow:
                if (followed) {
                    followBlogPresenter.unfollow(blogName);
                } else {
                    followBlogPresenter.follow(blogName);
                }
                return true;
            case R.id.post_filter:
                showFilterDialog();
                return true;
            case R.id.blog_likes:
                Intent intent = new Intent(this, LikesActivity.class);
                intent.putExtra("bundle", blogBundle);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onFollow() {
        if (followItem != null) {
            followItem.setTitle(R.string.blog_unfollow);
            followItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            followed = true;
        }
    }

    @Override
    public void onUnfollow() {
        if (followItem != null) {
            followItem.setTitle(R.string.blog_follow);
            followItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            followed = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        followBlogPresenter.onDetach();
    }

    public void hideFollowItem() {
        if (followItem != null) {
            followItem.setVisible(false);
        }
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setSingleChoiceItems(R.array.post_filter_type, presenter.getShowType(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                postListFragment.reloading(which);
            }
        });
        builder.create().show();
    }

}
