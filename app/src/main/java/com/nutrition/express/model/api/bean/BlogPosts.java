package com.nutrition.express.model.api.bean;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by huang on 2/19/16.
 */
public class BlogPosts {
    @SerializedName("blog")
    private BlogInfoItem blogInfo;
    @SerializedName("total_posts")
    private int count;
    @SerializedName("posts")
    private ArrayList<PostsItem> list;

    public int getCount() {
        return count;
    }

    public ArrayList<PostsItem> getList() {
        return list;
    }

    public BlogInfoItem getBlogInfo() {
        return blogInfo;
    }

}
