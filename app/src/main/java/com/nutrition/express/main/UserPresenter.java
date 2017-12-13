package com.nutrition.express.main;

import com.nutrition.express.model.data.DataManager;
import com.nutrition.express.model.rest.ApiService.UserService;
import com.nutrition.express.model.rest.RestCallback;
import com.nutrition.express.model.rest.RestClient;
import com.nutrition.express.model.rest.bean.BaseBean;
import com.nutrition.express.model.rest.bean.UserInfo;

import retrofit2.Call;

/**
 * Created by huang on 11/2/16.
 */

public class UserPresenter implements UserContract.Presenter {
    private UserService service;
    private UserContract.View view;
    private Call<BaseBean<UserInfo>> call;

    public UserPresenter(UserContract.View view) {
        this.view = view;
        service = RestClient.getInstance().getUserService();
    }

    @Override
    public void getMyInfo() {
        if (call == null) {
            call = service.getInfo();
            call.enqueue(new RestCallback<UserInfo>() {
                @Override
                public void onSuccess(UserInfo userInfo) {
                    if (view == null) {
                        return;
                    }
                    call = null;
                    view.showMyInfo(userInfo);
                    DataManager.getInstance().setUsers(userInfo.getUser());
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

    @Override
    public void onAttach(UserContract.View view) {

    }

    @Override
    public void onDetach() {
        view = null;
    }

}
