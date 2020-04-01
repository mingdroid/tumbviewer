package com.nutrition.express.model.api.bean;

/**
 * Created by huang on 4/24/16.
 */
public class TrailItem {
    private Blog blog;
    private Post post;
    private String content_raw;
    private String content;
    private boolean is_current_item;

    public Blog getBlog() {
        return blog;
    }

    public Post getPost() {
        return post;
    }

    public String getContent_raw() {
        return content_raw;
    }

    public String getContent() {
        return content;
    }

    public class Blog {
        private String name;
        private boolean active;
//        private Theme theme;
        private boolean share_likes;
        private boolean share_following;
        private boolean can_be_followed;

        public String getName() {
            return name;
        }

        public boolean isActive() {
            return active;
        }

//        public Theme getTheme() {
//            return theme;
//        }

        public boolean isShare_likes() {
            return share_likes;
        }

        public boolean isShare_following() {
            return share_following;
        }

        public boolean isCan_be_followed() {
            return can_be_followed;
        }
    }

    public class Post {
        private String id;

        public String getId() {
            return id;
        }
    }

    public class Theme {
        private int header_full_width;
        private int header_full_height;
        private int header_focus_width;
        private int header_focus_height;
        private String avatar_shapre;
        private String background_color;
        private String body_font;
        private String header_bounds;
        private String header_image;
        private String header_image_focused;
        private String header_image_scaled;
        private boolean header_stretch;
        private String link_color;
        private boolean show_avatar;
        private boolean show_description;
        private boolean show_header_image;
        private boolean show_title;
        private String title_color;
        private String title_font;
        private String title_font_weight;

        public int getHeader_full_width() {
            return header_full_width;
        }

        public int getHeader_full_height() {
            return header_full_height;
        }

        public int getHeader_focus_width() {
            return header_focus_width;
        }

        public int getHeader_focus_height() {
            return header_focus_height;
        }

        public String getAvatar_shapre() {
            return avatar_shapre;
        }

        public String getBackground_color() {
            return background_color;
        }

        public String getBody_font() {
            return body_font;
        }

        public String getHeader_bounds() {
            return header_bounds;
        }

        public String getHeader_image() {
            return header_image;
        }

        public String getHeader_image_focused() {
            return header_image_focused;
        }

        public String getHeader_image_scaled() {
            return header_image_scaled;
        }

        public boolean isHeader_stretch() {
            return header_stretch;
        }

        public String getLink_color() {
            return link_color;
        }

        public boolean isShow_avatar() {
            return show_avatar;
        }

        public boolean isShow_description() {
            return show_description;
        }

        public boolean isShow_header_image() {
            return show_header_image;
        }

        public boolean isShow_title() {
            return show_title;
        }

        public String getTitle_color() {
            return title_color;
        }

        public String getTitle_font() {
            return title_font;
        }

        public String getTitle_font_weight() {
            return title_font_weight;
        }
    }
}
