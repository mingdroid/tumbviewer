package com.nutrition.express.model.api.bean;

import java.util.List;

/**
 * Created by huang on 11/2/16.
 */

public class UserInfoItem {
    private long following;
    private long likes;
    private String default_post_format;
    private String name;
    private List<BlogInfoItem> blogs;

    public long getFollowing() {
        return following;
    }

    public long getLikes() {
        return likes;
    }

    public String getDefault_post_format() {
        return default_post_format;
    }

    public String getName() {
        return name;
    }

    public List<BlogInfoItem> getBlogs() {
        return blogs;
    }
}
