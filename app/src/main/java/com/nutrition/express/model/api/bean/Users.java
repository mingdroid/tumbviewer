package com.nutrition.express.model.api.bean;

import java.util.List;

/**
 * Created by huang on 10/18/16.
 */

public class Users {
    private int total_users;
    private List<UsersBean> users;

    public int getTotal_users() {
        return total_users;
    }

    public List<UsersBean> getUsers() {
        return users;
    }

    public static class UsersBean {
        private String name;
        private boolean following;
        private String url;
        private int updated;

        public String getName() {
            return name;
        }

        public boolean isFollowing() {
            return following;
        }

        public String getUrl() {
            return url;
        }

        public int getUpdated() {
            return updated;
        }
    }

}
