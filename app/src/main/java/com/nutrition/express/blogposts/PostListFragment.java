package com.nutrition.express.blogposts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nutrition.express.R;
import com.nutrition.express.common.CommonRVAdapter;
import com.nutrition.express.common.CommonViewHolder;
import com.nutrition.express.common.ExoPlayerInstance;
import com.nutrition.express.model.data.bean.PhotoPostsItem;
import com.nutrition.express.model.data.bean.VideoPostsItem;
import com.nutrition.express.useraction.DeletePostContract;
import com.nutrition.express.useraction.DeletePostPresenter;

/**
 * Created by huang on 5/26/16.
 */
public class PostListFragment extends Fragment
        implements CommonRVAdapter.OnLoadListener, PostContract.View, DeletePostContract.View {
    private RecyclerView recyclerView;
    private CommonRVAdapter adapter;
    private String blogName;
    private boolean loaded = false;
    private PostContract.Presenter presenter;
    private DeletePostPresenter deletePostPresenter;
    private DeleteReceiver deleteReceiver;
    private PostListActivity postListActivity;

    private ExoPlayerInstance playerInstance;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Bundle bundle = getArguments();
        blogName = bundle.getString("blog_name");
        postListActivity = (PostListActivity) context;

        if (playerInstance == null) {
            playerInstance = ExoPlayerInstance.getInstance();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isResumed() && !loaded) {
            getPostsVideo();
        }
        if (!isVisibleToUser && playerInstance != null) {
            playerInstance.pausePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint() && !loaded) {
            getPostsVideo();
        }
        playerInstance.resumePlayer();
    }

    @Override
    public void onStop() {
        super.onStop();
        playerInstance.releasePlayer();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = buildAdapter();
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.onDetach();
        }
        if (deletePostPresenter != null) {
            deletePostPresenter.onDetach();
        }
        if (deleteReceiver != null) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(deleteReceiver);
        }
    }

    @Override
    public void showData(Object[] items, boolean autoLoadingNext) {
        loaded = true;
        adapter.append(items, autoLoadingNext);
    }

    @Override
    public void onFollowed() {
        postListActivity.onFollow();
    }

    @Override
    public void onUnfollowed() {
        postListActivity.onUnfollow();
    }

    /**
     * It means showing the user's posts list.
     */
    @Override
    public void hideFollowItem() {
        postListActivity.hideFollowItem();
        deleteReceiver = new DeleteReceiver();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(deleteReceiver,
                new IntentFilter(DeletePostPresenter.ACTION_DELETE_POST));
    }

    @Override
    public void onError(int code, String error) {
        adapter.showLoadingFailure(getString(R.string.load_failure_des, code, error));
    }

    @Override
    public void retry() {
        getPostsVideo();
    }

    @Override
    public void loadNextPage() {
        getPostsVideo();
    }

    @Override
    public void resetData(Object[] items, boolean hasNext) {
        adapter.resetData(items, hasNext);
    }

    private void getPostsVideo() {
        if (presenter == null) {
            presenter = new PostPresenter(this);
        }
        presenter.loadData(blogName);
    }

    private CommonRVAdapter buildAdapter() {
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

    public void setPresenter(PostContract.Presenter presenter) {
        this.presenter = presenter;
        this.presenter.onAttach(this);
    }

    public void reloading(int type) {
        presenter.setShowType(type);
        adapter.showReloading();
    }

    public void scrollToTop() {
        recyclerView.scrollToPosition(0);
    }

    @Override
    public void onDeletePostSuccess(int position) {
        adapter.remove(position);
    }

    private class DeleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (deletePostPresenter == null) {
                deletePostPresenter = new DeletePostPresenter(PostListFragment.this);
            }
            String name = intent.getStringExtra("name");
            String id = intent.getStringExtra("id");
            int position = intent.getIntExtra("position", 0);
            deletePostPresenter.deletePost(name, id, position);
        }
    }

}
