package com.example.videoapp.Favorites;

import android.content.Context;

import com.example.videoapp.HotVideo.Model.Video;
import com.example.videoapp.SQL.SQLiteVideo;
import java.util.ArrayList;

public class FavoritePresenter implements IFavorite.Presenter {
    IFavorite.View view;
    Context context;

    public FavoritePresenter(Context context) {
        this.context = context;
    }

    public void attachView(FavoritesFragment view){
        this.view = view;
    }
    public void detachView(){
        this.view = null;
    }

    @Override
    public void getDataFromSQL() {
        SQLiteVideo sqLiteVideo = new SQLiteVideo(context);
        ArrayList<Video> favoriteList = sqLiteVideo.getAllVideo();
        view.showFavoriteVideo(favoriteList);
    }
}
