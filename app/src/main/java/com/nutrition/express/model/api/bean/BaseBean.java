package com.nutrition.express.model.api.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Created by huang on 2/17/16.
 */
public class BaseBean<T> {
    @SerializedName("meta")
    private BaseMeta meta;
    @SerializedName("response")
    private T response;

    public BaseMeta getMeta() {
        return meta;
    }

    public T getResponse() {
        return response;
    }
}
