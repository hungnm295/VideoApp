package com.example.videoapp.HotVideo.Presenter;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.videoapp.Categories.View.ItemCategoryFragment;
import com.example.videoapp.model.GetDataHelper;
import com.example.videoapp.model.Video;
import com.example.videoapp.model.sql.SQLiteVideo;

import java.util.ArrayList;


public class CategoryVideoPresenter implements IVideo.Presenter{
    private IVideo.View view;
    ArrayList<Video> videoList;
    GetDataHelper getVideoData;
    SQLiteVideo sqLiteVideo;

    public void attachedViewVideo(ItemCategoryFragment view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    /*@Override
    public void fetchDataFromSever(String url) {
        new getVideoData(url).execute();
    }*/

    @Override
    public void fetchDataFromSever(String url) {
        new getVideoData(url).execute();
    }

    @Override
    public void addFavoriteVideo(Video video, Context context) {
        sqLiteVideo = new SQLiteVideo(context);
        sqLiteVideo.insertVideo(video);
        Toast.makeText(context, "Added to Favorite", Toast.LENGTH_SHORT).show();
    }

    class getVideoData extends AsyncTask<Void, Void, Void> {
        String url;

        public getVideoData(String url) {
            this.url = url;
        }

        @Override
        protected void onPreExecute() {
            view.showProgressBar(true);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getVideoData = new GetDataHelper();
            videoList = getVideoData.getVideoList(url);
            //videoList = GetDataHelper.getInstance().getObjectFromJSon(url);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            view.showProgressBar(false);
            if (view != null) {
                view.showContent(videoList);
            }
            super.onPostExecute(aVoid);
        }
    }

}
