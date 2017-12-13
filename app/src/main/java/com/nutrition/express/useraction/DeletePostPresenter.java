package com.nutrition.express.useraction;

import com.nutrition.express.model.rest.ApiService.BlogService;
import com.nutrition.express.model.rest.RestCallback;
import com.nutrition.express.model.rest.RestClient;
import com.nutrition.express.model.rest.bean.BaseBean;

import retrofit2.Call;

/**
 * Created by huang on 12/12/16.
 */

public class DeletePostPresenter implements DeletePostContract.Presenter {
    public static final String ACTION_DELETE_POST = "DELETE_POST";
    private DeletePostContract.View view;
    private BlogService service;
    private Call<BaseBean<Void>> call;

    public DeletePostPresenter(DeletePostContract.View view) {
        this.view = view;
        service = RestClient.getInstance().getBlogService();
    }

    @Override
    public void onAttach(DeletePostContract.View view) {

    }

    @Override
    public void onDetach() {
        view = null;
    }

    @Override
    public void deletePost(String blogName, String postId, final int position) {
        if (call == null) {
            call = service.deletePost(blogName, postId);
            call.enqueue(new RestCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if (view == null) {
                        return;
                    }
                    call = null;
                    view.onDeletePostSuccess(position);
                }

                @Override
                public void onError(int code, String message) {
                    if (view == null) {
                        return;
                    }
                    call = null;
                    view.onError(code, message);
                }
            });
        }
    }

}
