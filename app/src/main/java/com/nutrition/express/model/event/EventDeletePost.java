package com.nutrition.express.model.event;

/**
 * Created by huang on 12/13/17.
 */

public class EventDeletePost {
    private String name;
    private String id;
    private int position;

    public EventDeletePost(String name, String id, int position) {
        this.name = name;
        this.id = id;
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public int getPosition() {
        return position;
    }
}
