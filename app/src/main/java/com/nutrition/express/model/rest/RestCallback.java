package com.nutrition.express.model.rest;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.nutrition.express.BuildConfig;
import com.nutrition.express.model.Error;
import com.nutrition.express.model.data.DataManager;
import com.nutrition.express.model.event.EventError401;
import com.nutrition.express.model.event.EventError429;
import com.nutrition.express.model.rest.bean.BaseBean;
import com.nutrition.express.model.rest.bean.ErrorBean;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by huang on 2/18/16.
 */
public abstract class RestCallback<T> implements Callback<BaseBean<T>> {

    public RestCallback() {
    }

    @Override
    public void onResponse(Call<BaseBean<T>> call, Response<BaseBean<T>> response) {
        if (response.isSuccessful()) {
            // status code [200, 299]
            BaseBean<T> body = response.body();
            onSuccess(body.getResponse());
        } else {
            // status code [400, 599]
            // the server return a JsonObject that contain error message.
            try {
                ErrorBean errorBean = new Gson().fromJson(response.errorBody().string(),
                        new TypeToken<ErrorBean>(){}.getType());
                handleError(errorBean);
            } catch (IOException | JsonSyntaxException e) {
                onError(response.code(), response.message());
            }
        }
        Headers headers = response.headers();
        DataManager.getInstance().updateTumblrAppInfo(
                headers.get("X-RateLimit-PerDay-Limit"),
                headers.get("X-RateLimit-PerDay-Remaining"),
                headers.get("X-RateLimit-PerDay-Reset"),
                headers.get("X-RateLimit-PerHour-Limit"),
                headers.get("X-RateLimit-PerHour-Remaining"),
                headers.get("X-RateLimit-PerHour-Reset"));
    }

    @Override
    public void onFailure(Call<BaseBean<T>> call, Throwable t) {
        if (BuildConfig.DEBUG) {
            t.printStackTrace();
        }
        if (TextUtils.equals(t.getMessage(), "Canceled")) {
            onError(0, "Canceled, touch to retry");
        } else {
            onError(Error.ERROR_REST_FAILURE, t.getMessage());
        }
    }

    private void handleError(ErrorBean errorBean) {
        synchronized (RestCallback.class) {
            if (errorBean.getMeta().getStatus() == 401) {
                RestClient.getInstance().cancelAllCall();
                //Unauthorized, need login again
                DataManager dataManager = DataManager.getInstance();
                dataManager.removeAccount(dataManager.getPositiveAccount());
                if (dataManager.switchToNextRoute()) {
                    onError(Error.ERROR_REST_RETRY, "Failed, touch to retry");
                } else {
                    EventBus.getDefault().post(new EventError401());
                }
            } else if (errorBean.getMeta().getStatus() == 429) {
                RestClient.getInstance().cancelAllCall();
                //429 request limit exceeded
                DataManager dataManager = DataManager.getInstance();
                if (dataManager.switchToNextRoute()) {
                    onError(Error.ERROR_REST_RETRY, "Failed, touch to retry");
                } else {
                    EventBus.getDefault().post(new EventError429());
                }
            } else {
                onError(errorBean.getMeta().getStatus(), errorBean.getMeta().getMsg());
            }
        }
    }

    public abstract void onSuccess(T t);
    public abstract void onError(int code, String message);

}
