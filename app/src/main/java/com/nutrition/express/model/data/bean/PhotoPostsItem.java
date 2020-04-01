package com.nutrition.express.model.data.bean;

import com.nutrition.express.model.api.bean.PostsItem;

/**
 * Created by huang on 2/23/17.
 */

public class PhotoPostsItem {
    protected PostsItem postsItem;

    public PhotoPostsItem(PostsItem postsItem) {
        this.postsItem = postsItem;
    }

    public PostsItem getPostsItem() {
        return postsItem;
    }
}
