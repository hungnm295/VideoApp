package com.example.videoapp.view.videohot;

import android.content.Context;

import com.example.videoapp.model.object.Video;

import java.util.ArrayList;

public interface IVideo {
    //Nên để mỗi view sẽ để 1 adapter riêng
    interface OnClick { //Đặt tên Interface có chữ I ở đầu tiên
        void onClickVideo(int position);
        void onClickMenu(int position, android.view.View view);
    }

    interface View {
        //Show các tính năng ở đây
        //ShowProgressbar chỉ là hàm
        void showProgressBar(boolean isLoading); //OnMessage
        void showContent(ArrayList<Video> videoList); //OnSuccessful
    }

    interface Presenter {
        void fetchDataFromSever(String url);
        void addFavoriteVideo(Video video, Context context);
    }
}
