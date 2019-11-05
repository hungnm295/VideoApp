package com.example.videoapp.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class Video implements Serializable {
    @SerializedName("id")
    @Expose
    String id;
    @SerializedName("avatar")
    @Expose
    String avatar;
    @SerializedName("file_mp4")
    @Expose
    String file_mp4;
    @SerializedName("title")
    @Expose
    String title;
    @SerializedName("duration")
    @Expose
    String duration;
    @SerializedName("date_published")
    @Expose
    String date_published;

    public Video(String id, String avatar, String file_mp4, String title, String duration, String date_published) {
        this.id = id;
        this.avatar = avatar;
        this.file_mp4 = file_mp4;
        this.title = title;
        this.duration = duration;
        this.date_published = date_published;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getFile_mp4() {
        return file_mp4;
    }

    public void setFile_mp4(String file_mp4) {
        this.file_mp4 = file_mp4;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDate_published() {
        return date_published;
    }

    public void setDate_published(String date_published) {
        this.date_published = date_published;
    }

}
