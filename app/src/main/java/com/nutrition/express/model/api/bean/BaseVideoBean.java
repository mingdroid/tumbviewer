package com.nutrition.express.model.api.bean;

import android.net.Uri;

/**
 * Created by huang on 2/23/17.
 */

public class BaseVideoBean {
    protected Uri thumbnailUri;
    protected Uri sourceUri;
    protected int width;
    protected int height;

    public BaseVideoBean() {
    }

    public Uri getThumbnailUri() {
        return thumbnailUri;
    }

    public Uri getSourceUri() {
        return sourceUri;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
