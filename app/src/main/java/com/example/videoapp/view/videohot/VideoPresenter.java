package com.example.videoapp.view.videohot;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.videoapp.R;
import com.example.videoapp.model.network.GetDataHelper;
import com.example.videoapp.model.object.Video;
import com.example.videoapp.model.sql.SQLiteVideo;

import java.util.ArrayList;


public class VideoPresenter implements IVideo.Presenter{
    private IVideo.View view;
    ArrayList<Video> videoList;
    SQLiteVideo sqLiteVideo;

    public void attachedViewVideo(HotVideoFragment view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    @Override
    public void fetchDataFromSever(String url) {
            new getVideoData(url).execute();
    }

    @Override
    public void addFavoriteVideo(Video video, Context context) {
        sqLiteVideo = new SQLiteVideo(context);
        if(!sqLiteVideo.ifExists(video)){
            sqLiteVideo.insertVideo(video);
            Toast.makeText(context, context.getResources().getString(R.string.added), Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, context.getResources().getString(R.string.existed), Toast.LENGTH_SHORT).show();
        }

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
            videoList = GetDataHelper.getInstance().getVideoList(url);
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
