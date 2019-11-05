package com.example.videoapp.model;

import com.example.videoapp.Constant;
import com.example.videoapp.model.Video;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;


public interface RequestAPI {
    @Headers("Content-Type: application/json")
    @GET(Constant.HOTVIDEO_GET)
    Call<ArrayList<Video>> getVideoData();
}
