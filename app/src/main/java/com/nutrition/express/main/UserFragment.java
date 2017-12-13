package com.nutrition.express.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.nutrition.express.R;
import com.nutrition.express.blogposts.PostListActivity;
import com.nutrition.express.common.CommonRVAdapter;
import com.nutrition.express.common.CommonViewHolder;
import com.nutrition.express.download.DownloadManagerActivity;
import com.nutrition.express.following.FollowingActivity;
import com.nutrition.express.likes.LikesActivity;
import com.nutrition.express.model.data.DataManager;
import com.nutrition.express.model.rest.bean.BlogInfoItem;
import com.nutrition.express.model.rest.bean.UserInfo;
import com.nutrition.express.settings.SettingsActivity;
import com.nutrition.express.util.FrescoUtils;

import java.util.ArrayList;

/**
 * Created by huang on 11/2/16.
 */

public class UserFragment extends Fragment implements UserContract.View, View.OnClickListener {
    private UserContract.Presenter presenter;
    private boolean loaded = false;

    private TextView likeTV, followingTV;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        likeTV = (TextView) view.findViewById(R.id.like_text);
        followingTV = (TextView) view.findViewById(R.id.following_text);
        recyclerView = (RecyclerView) view.findViewById(R.id.user_blog_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        view.findViewById(R.id.like_layout).setOnClickListener(this);
        view.findViewById(R.id.following_layout).setOnClickListener(this);
        view.findViewById(R.id.setting_layout).setOnClickListener(this);
        view.findViewById(R.id.download_layout).setOnClickListener(this);

        presenter = new UserPresenter(this);
        loaded = false;
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!loaded && isVisibleToUser && isResumed()) {
            getUserInfo();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!loaded) {
            getUserInfo();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.onDetach();
        }
    }

    @Override
    public void showMyInfo(UserInfo info) {
        loaded = true;
        likeTV.setText(Long.toString(info.getUser().getLikes()));
        followingTV.setText(Long.toString(info.getUser().getFollowing()));

        CommonRVAdapter.Builder builder = CommonRVAdapter.newBuilder();
        builder.addItemType(BlogInfoItem.class, R.layout.item_user_blog,
                new CommonRVAdapter.CreateViewHolder() {
                    @Override
                    public CommonViewHolder createVH(View view) {
                        return new BlogVH(view);
                    }
                });
        builder.setData(new ArrayList<Object>(info.getUser().getBlogs()));
        recyclerView.setAdapter(builder.build());
        DataManager.getInstance().setUsers(info.getUser());
    }

    @Override
    public void onError(int code, String error) {
        loaded = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.like_layout:
                Intent likeIntent = new Intent(getActivity(), LikesActivity.class);
                startActivity(likeIntent);
                break;
            case R.id.following_layout:
                Intent intent = new Intent(getActivity(), FollowingActivity.class);
                startActivity(intent);
                break;
            case R.id.setting_layout:
                Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.download_layout:
                Intent downloadIntent = new Intent(getActivity(), DownloadManagerActivity.class);
                startActivity(downloadIntent);
                break;
        }
    }

    private void getUserInfo() {
        if (presenter == null) {
            presenter = new UserPresenter(this);
        }
        presenter.getMyInfo();
    }

    public void refreshData() {
        loaded = false;
    }

    private class BlogVH extends CommonViewHolder<BlogInfoItem> implements View.OnClickListener {
        private SimpleDraweeView avatarView;
        private TextView titleTV, nameTV;

        public BlogVH(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            avatarView = (SimpleDraweeView) itemView.findViewById(R.id.blog_avatar);
            titleTV = (TextView) itemView.findViewById(R.id.blog_title);
            nameTV = (TextView) itemView.findViewById(R.id.blog_name);
        }

        @Override
        public void bindView(BlogInfoItem infoItem) {
            titleTV.setText(infoItem.getTitle());
            nameTV.setText(infoItem.getName());
            FrescoUtils.setTumblrAvatarUri(avatarView, infoItem.getName(), 128);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), PostListActivity.class);
            intent.putExtra("blog_name", nameTV.getText().toString());
            intent.putExtra("is_admin", true);
            startActivity(intent);
        }
    }

}
