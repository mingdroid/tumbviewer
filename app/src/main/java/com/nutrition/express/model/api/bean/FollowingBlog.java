package com.nutrition.express.model.api.bean;

import java.util.List;

/**
 * Created by huang on 11/2/16.
 */

public class FollowingBlog {
    private long total_blogs;
    private List<Blog> blogs;

    public long getTotal_blogs() {
        return total_blogs;
    }

    public List<Blog> getBlogs() {
        return blogs;
    }

    public static class Blog {
        private String name;
        private String url;
        private long updated;
        private String title;
        private String description;

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public long getUpdated() {
            return updated;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }
    }
}
