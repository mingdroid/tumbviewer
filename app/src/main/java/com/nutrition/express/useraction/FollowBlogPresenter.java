package com.nutrition.express.useraction;

import com.nutrition.express.model.rest.ApiService.UserService;
import com.nutrition.express.model.rest.RestCallback;
import com.nutrition.express.model.rest.RestClient;
import com.nutrition.express.model.rest.bean.BaseBean;

import retrofit2.Call;

/**
 * Created by huang on 11/7/16.
 */

public class FollowBlogPresenter implements FollowBlogContract.Presenter {
    private FollowBlogContract.View view;
    private UserService service;
    private Call<BaseBean<Void>> call;

    public FollowBlogPresenter(FollowBlogContract.View view) {
        this.view = view;
        service = RestClient.getInstance().getUserService();
    }

    public void follow(String url) {
        if (call == null) {
            call = service.follow(url);
            call.enqueue(new RestCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if (view == null) {
                        return;
                    }
                    call = null;
                    view.onFollow();
                }

                @Override
                public void onError(int code, String message) {
                    if (view == null) {
                        return;
                    }
                    call = null;
                }
            });
        }
    }

    @Override
    public void unfollow(String url) {
        if (call == null) {
            call = service.unfollow(url);
            call.enqueue(new RestCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if (view == null) {
                        return;
                    }
                    call = null;
                    view.onUnfollow();
                }

                @Override
                public void onError(int code, String message) {
                    if (view == null) {
                        return;
                    }
                    call = null;
                }
            });
        }
    }

    @Override
    public void onAttach(FollowBlogContract.View view) {

    }

    @Override
    public void onDetach() {
        view = null;
    }

}
