package com.nutrition.express.taggedposts;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.nutrition.express.R;
import com.nutrition.express.blogposts.PhotoPostVH;
import com.nutrition.express.blogposts.VideoPhotoPostVH;
import com.nutrition.express.common.CommonRVAdapter;
import com.nutrition.express.common.CommonViewHolder;
import com.nutrition.express.common.ExoPlayerInstance;
import com.nutrition.express.model.data.bean.PhotoPostsItem;
import com.nutrition.express.model.data.bean.VideoPostsItem;

import java.util.List;

/**
 * Created by huang on 11/21/16.
 */

public class TaggedActivity extends AppCompatActivity implements TaggedContract.View,
        CommonRVAdapter.OnLoadListener {
    private TaggedContract.Presenter presenter;
    private CommonRVAdapter adapter;
    private String tag;

    private ExoPlayerInstance playerInstance;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tagged);

        Intent intent = getIntent();
        tag = intent.getStringExtra("tag");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(tag);
        }
        adapter = getAdapter();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        presenter = new TaggedPresenter(this);
        presenter.getTaggedPosts(tag);

        playerInstance = ExoPlayerInstance.getInstance();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onResume() {
        super.onResume();
        playerInstance.resumePlayer();
    }

    @Override
    public void onStop() {
        super.onStop();
        playerInstance.releasePlayer();
    }

    private CommonRVAdapter getAdapter() {
        CommonRVAdapter.Builder builder = CommonRVAdapter.newBuilder();
        builder.addItemType(PhotoPostsItem.class, R.layout.item_post,
                new CommonRVAdapter.CreateViewHolder() {
                    @Override
                    public CommonViewHolder createVH(View view) {
                        return new PhotoPostVH(view);
                    }
                });
        builder.addItemType(VideoPostsItem.class, R.layout.item_video_post,
                new CommonRVAdapter.CreateViewHolder() {
                    @Override
                    public CommonViewHolder createVH(View view) {
                        return new VideoPhotoPostVH(view, playerInstance);
                    }
                });
        builder.setLoadListener(this);
        return builder.build();
    }

    @Override
    public void onError(int code, String error) {
        adapter.showLoadingFailure(getString(R.string.load_failure_des, code, error));
    }

    @Override
    public void showTaggedPosts(List<PhotoPostsItem> postsItems, boolean hasNext) {
        adapter.append(postsItems.toArray(), hasNext);
    }

    @Override
    public void retry() {
        presenter.getTaggedPosts(tag);
    }

    @Override
    public void loadNextPage() {
        presenter.nextTaggedPosts(tag);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDetach();
    }

}
