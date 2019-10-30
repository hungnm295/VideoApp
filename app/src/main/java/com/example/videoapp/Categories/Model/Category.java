package com.example.videoapp.Categories.Model;

public class Category {
    String urlThumb;
    String title;

    public Category(String urlThumb, String title) {
        this.urlThumb = urlThumb;
        this.title = title;
    }

    public String getUrlThumb() {
        return urlThumb;
    }

    public void setUrlThumb(String urlThumb) {
        this.urlThumb = urlThumb;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
