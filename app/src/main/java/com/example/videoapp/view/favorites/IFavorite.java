package com.example.videoapp.view.favorites;

import com.example.videoapp.model.object.Video;

import java.util.ArrayList;

public interface IFavorite {
    interface View{
        void showFavoriteVideo(ArrayList<Video> favoriteList);
    }
    interface Presenter{
        void getDataFromSQL();
    }
}
