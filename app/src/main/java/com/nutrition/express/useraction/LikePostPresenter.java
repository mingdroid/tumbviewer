package com.nutrition.express.useraction;

import com.nutrition.express.model.rest.ApiService.UserService;
import com.nutrition.express.model.rest.RestCallback;
import com.nutrition.express.model.rest.RestClient;
import com.nutrition.express.model.rest.bean.BaseBean;

import retrofit2.Call;

/**
 * Created by huang on 11/7/16.
 */

public class LikePostPresenter implements LikePostContract.Presenter {
    private LikePostContract.View view;
    private UserService userService;
    private Call<BaseBean<Void[]>> call;

    public LikePostPresenter(LikePostContract.View view) {
        this.view = view;
        userService = RestClient.getInstance().getUserService();
    }

    @Override
    public void like(final long id, String reblogKey) {
        if (call == null) {
            call = userService.like(id, reblogKey);
            call.enqueue(new RestCallback<Void[]>() {
                @Override
                public void onSuccess(Void[] voids) {
                    if (null == view) {
                        return;
                    }
                    call = null;
                    view.onLike(id);
                }

                @Override
                public void onError(int code, String message) {
                    if (null == view) {
                        return;
                    }
                    call = null;
                    view.onLikeFailure();
                }
            });
        }
    }

    @Override
    public void unlike(final long id, String reblogKey) {
        if (call == null) {
            call = userService.unlike(id, reblogKey);
            call.enqueue(new RestCallback<Void[]>() {
                @Override
                public void onSuccess(Void[] voids) {
                    if (null == view) {
                        return;
                    }
                    call = null;
                    view.onUnlike(id);
                }

                @Override
                public void onError(int code, String message) {
                    if (null == view) {
                        return;
                    }
                    call = null;
                    view.onUnlikeFailure();
                }
            });
        }
    }

    @Override
    public void onAttach(LikePostContract.View view) {

    }

    @Override
    public void onDetach() {
        view = null;
    }

}
