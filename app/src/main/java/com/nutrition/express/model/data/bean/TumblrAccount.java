package com.nutrition.express.model.data.bean;

/**
 * Created by huang on 1/4/17.
 * ////  2/11/17 wrong design, you don't the new login account whether the same as current
 */

public class TumblrAccount {
    private String apiKey;
    private String apiSecret;
    private String token;
    private String secret;
    private String name = null; // The user's tumblr short name
    private boolean isUsing = false;
    private transient boolean limitExceeded;

    public TumblrAccount(String apiKey, String apiSecret, String token, String secret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.token = token;
        this.secret = secret;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public String getToken() {
        return token;
    }

    public String getSecret() {
        return secret;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUsing() {
        return isUsing;
    }

    public void setUsing(boolean using) {
        isUsing = using;
    }

    public boolean isLimitExceeded() {
        return limitExceeded;
    }

    public void setLimitExceeded(boolean limitExceeded) {
        this.limitExceeded = limitExceeded;
    }
}
