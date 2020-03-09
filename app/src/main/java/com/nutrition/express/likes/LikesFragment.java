package com.nutrition.express.likes;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
 * Created by huang on 11/8/16.
 */

public class LikesFragment extends Fragment
        implements LikesContract.View, CommonRVAdapter.OnLoadListener {
    private CommonRVAdapter adapter;
    private LikesContract.LikesPresenter presenter;
    private String blogName = null;

    private ExoPlayerInstance playerInstance;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (playerInstance == null) {
            playerInstance = ExoPlayerInstance.getInstance();
        }
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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser && playerInstance != null) {
            playerInstance.pausePlayer();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_likes, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = getCommonRVAdapter();
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        presenter = new LikesPresenter(this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            blogName = bundle.getString("blog_name", null);
        }
        if (blogName == null) {
            presenter.getLikePosts();
        } else {
            presenter.getLikePosts(blogName);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.onDetach();
        }
    }

    private CommonRVAdapter getCommonRVAdapter() {
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
    public void retry() {
        if (blogName == null) {
            presenter.getLikePosts();
        } else {
            presenter.getLikePosts(blogName);
        }
    }

    @Override
    public void loadNextPage() {
        presenter.nextLikePosts();
    }

    @Override
    public void showLikePosts(List<PhotoPostsItem> posts, boolean hasNext) {
        adapter.append(posts.toArray(), hasNext);
    }

    @Override
    public void onError(int code, String error) {
        adapter.showLoadingFailure(getString(R.string.load_failure_des, code, error));
    }

}
