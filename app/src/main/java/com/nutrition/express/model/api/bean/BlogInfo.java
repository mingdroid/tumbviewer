package com.nutrition.express.model.api.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Created by huang on 2/17/16.
 */
public class BlogInfo {
    @SerializedName("blog")
    private BlogInfoItem item;

    public BlogInfoItem getItem() {
        return item;
    }
}
