package com.example.videoapp.HotVideo.Model;

import com.example.videoapp.Constant;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;


public interface RequestAPI {
    @Headers("Content-Type: application/json")
    @GET(Constant.HOTVIDEO_GET)
    Call<ArrayList<Video>> getVideoData();
}
