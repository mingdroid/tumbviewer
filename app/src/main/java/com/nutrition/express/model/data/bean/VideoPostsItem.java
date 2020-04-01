package com.nutrition.express.model.data.bean;

import com.nutrition.express.model.api.bean.PostsItem;

/**
 * Created by huang on 2/23/17.
 */

public class VideoPostsItem extends PhotoPostsItem {
    private OnlineVideo onlineVideo;

    public VideoPostsItem(PostsItem postsItem) {
        super(postsItem);
    }

    public OnlineVideo getOnlineVideo() {
        if (onlineVideo == null) {
            onlineVideo = new OnlineVideo(postsItem);
        }
        return onlineVideo;
    }
}
