package com.example.videoapp.HotVideo.Presenter;

import android.content.Context;

import com.example.videoapp.HotVideo.Model.Video;

import java.util.ArrayList;
import java.util.List;

public interface IVideo {
    interface OnClick {
        void onClickVideo(int position);
        void onClickMenu(int position, android.view.View view);
    }

    interface View {
        void showProgressBar();
        void hideProgressBar();
        void showContent(ArrayList<Video> videoList);
    }

    interface Presenter {
        void fetchDataFromSever(String url);
        void addFavoriteVideo(Video video, Context context);
    }

}
