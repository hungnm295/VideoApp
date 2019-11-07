package com.example.videoapp.model.retrofit;

import com.example.videoapp.utilities.Constant;
import com.example.videoapp.model.object.Video;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;


public interface RequestAPI {
    @Headers("Content-Type: application/json")
    @GET(Constant.HOTVIDEO_GET)
    Call<ArrayList<Video>> getVideoData();
}
