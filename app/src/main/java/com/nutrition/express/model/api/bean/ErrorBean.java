package com.nutrition.express.model.api.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Created by huang on 11/9/16.
 */

public class ErrorBean {
    @SerializedName("meta")
    private BaseMeta meta;

    public BaseMeta getMeta() {
        return meta;
    }
}
