package com.nutrition.express.model.api.bean;

import java.util.List;

/**
 * Created by huang on 10/19/16.
 */

public class PhotoItem {
    private String caption;
    private List<PhotoInfo> alt_sizes;
    private PhotoInfo original_size;

    public String getCaption() {
        return caption;
    }

    public List<PhotoInfo> getAlt_sizes() {
        return alt_sizes;
    }

    public PhotoInfo getOriginal_size() {
        return original_size;
    }

    public class PhotoInfo {
        private String url;
        private int width;
        private int height;

        public String getUrl() {
            return url;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
}
