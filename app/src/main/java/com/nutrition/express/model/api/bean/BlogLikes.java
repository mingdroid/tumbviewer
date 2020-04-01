package com.nutrition.express.model.api.bean;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by huang on 2/18/16.
 */
public class BlogLikes {
    @SerializedName("liked_count")
    private int count;
    @SerializedName("liked_posts")
    private ArrayList<PostsItem> list;

    public int getCount() {
        return count;
    }

    public ArrayList<PostsItem> getList() {
        return list;
    }
}
