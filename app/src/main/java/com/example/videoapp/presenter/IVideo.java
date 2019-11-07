package com.example.videoapp.presenter;

import android.content.Context;

import com.example.videoapp.model.object.Video;

import java.util.ArrayList;

public interface IVideo {
    interface OnClick {
        void onClickVideo(int position);
        void onClickMenu(int position, android.view.View view);
    }

    interface View {
        void showProgressBar(boolean isLoading);
        void showContent(ArrayList<Video> videoList);
    }

    interface Presenter {
        void fetchDataFromSever(String url);
        void addFavoriteVideo(Video video, Context context);
    }

}
