package com.example.videoapp.Favorites;

import com.example.videoapp.model.Video;

import java.util.ArrayList;

public interface IFavorite {
    interface View{
        void showFavoriteVideo(ArrayList<Video> favoriteList);
    }
    interface Presenter{
        void getDataFromSQL();
    }
}
