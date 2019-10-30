package com.example.videoapp.Favorites;

import com.example.videoapp.HotVideo.Model.Video;

import java.util.ArrayList;

public interface IFavorite {
    interface View{
        void showFavoriteVideo(ArrayList<Video> favoriteList);
    }
    interface Presenter{
        void getDataFromSQL();
    }
}
