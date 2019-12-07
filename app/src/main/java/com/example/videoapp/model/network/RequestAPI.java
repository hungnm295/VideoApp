package com.example.videoapp.model.network;

import com.example.videoapp.util.Constant;
import com.example.videoapp.model.object.Video;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;


public interface RequestAPI {
    @Headers("Content-Type: application/json")
    @GET(Constant.HOTVIDEO_GET)
    Call<List<Video>> getVideoData();
}
