package com.example.videoapp.HotVideo.Presenter;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.videoapp.model.GetDataHelper;
import com.example.videoapp.model.Video;
import com.example.videoapp.HotVideo.View.HotVideoFragment;
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

        /*HttpHelper.getInstance().getRequestAPI().getVideoData().enqueue(new Callback<ArrayList<Video>>() {
            // Đầu tiên lấy Instance của HttpHelper --> getRequestAPI --> call getVideoData
            @Override
            public void onResponse(Call<ArrayList<Video>> call, Response<ArrayList<Video>> response) {
                if (view != null) {
                    Log.d("CheckVideoResponse", "Success VideoSize: " + response.body().size());
                    view.showContent(response.body());
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Video>> call, Throwable t) {
                Log.d("CheckVideoResponse", "Error: " + t.getMessage());
            }
        });*/
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
