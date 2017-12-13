package com.nutrition.express.following;

import com.nutrition.express.model.rest.ApiService.UserService;
import com.nutrition.express.model.rest.RestCallback;
import com.nutrition.express.model.rest.RestClient;
import com.nutrition.express.model.rest.bean.BaseBean;
import com.nutrition.express.model.rest.bean.FollowingBlog;

import retrofit2.Call;

/**
 * Created by huang on 10/18/16.
 */

public class FollowingPresenter implements FollowingContract.FollowersPresenter {
    private FollowingContract.View view;
    private UserService service;
    private Call<BaseBean<FollowingBlog>> followingCall;
    private int defaultLimit = 20;
    private int offset = 0;
    private boolean hasNext = true;

    public FollowingPresenter(FollowingContract.View view) {
        this.view = view;
        service = RestClient.getInstance().getUserService();
    }

    @Override
    public void getMyFollowing() {
        getFollowingBlog();
    }

    @Override
    public void getNextFollowing() {
        getFollowingBlog();
    }

    private void getFollowingBlog() {
        if (followingCall == null) {
            followingCall = service.getFollowing(defaultLimit, offset);
            followingCall.enqueue(new RestCallback<FollowingBlog>() {
                @Override
                public void onSuccess(FollowingBlog blogs) {
                    if (view == null) {
                        return;
                    }
                    followingCall = null;
                    offset += blogs.getBlogs().size();
                    if (blogs.getBlogs().size() < defaultLimit || offset >= blogs.getTotal_blogs()) {
                        hasNext = false;
                    }
                    view.showFollowing(blogs.getBlogs(), hasNext);
                }

                @Override
                public void onError(int code, String message) {
                    if (view == null) {
                        return;
                    }
                    followingCall = null;
                    view.onError(code, message);
                }
            });
        }
    }

    @Override
    public void onAttach(FollowingContract.View view) {

    }

    @Override
    public void onDetach() {
        view = null;
    }

}
