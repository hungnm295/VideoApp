package com.example.videoapp.HotVideo.Model;

import android.content.res.Resources;

import com.example.videoapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import retrofit2.http.GET;

public class GetVideoData {
    private static GetVideoData INSTANCE;
    public synchronized static GetVideoData getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new GetVideoData();
        }
        return INSTANCE;
    }
    public ArrayList<Video> getObjectFromJSon(String urlVideo) {
        String result = "";
        ArrayList<Video> videoList = new ArrayList<>();
        try {
            URL url = new URL(urlVideo);
            URLConnection connection = url.openConnection();
            InputStream in = connection.getInputStream();
            int byteCharacter;
            while ((byteCharacter = in.read()) != -1) {
                result += (char) byteCharacter;
            }
            JSONArray jsonArray = new JSONArray(result);
            int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                JSONObject jsonVideo = jsonArray.getJSONObject(i);
                String id = jsonVideo.getString("id");
                String avatarURL = jsonVideo.getString("avatar");
                String videoURL = jsonVideo.getString("file_mp4");
                String title = jsonVideo.getString("title");
                String duration = jsonVideo.getString("duration");
                String datePublish = jsonVideo.getString("date_published");
                videoList.add(new Video(id, avatarURL, videoURL, title, duration, datePublish));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return videoList;
    }

}
