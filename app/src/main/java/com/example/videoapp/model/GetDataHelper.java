package com.example.videoapp.model;

import com.example.videoapp.model.object.Category;
import com.example.videoapp.model.object.Video;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class GetDataHelper {
    private static GetDataHelper INSTANCE;

    public synchronized static GetDataHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GetDataHelper();
        }
        return INSTANCE;
    }

    public String getJSon(String url1) {
        String result = "";
        try {
            URL url = new URL(url1);
            URLConnection connection = url.openConnection();
            InputStream in = connection.getInputStream();
            int byteCharacter;
            while ((byteCharacter = in.read()) != -1) {
                result += (char) byteCharacter;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public ArrayList<Video> getVideoList(String url) {
        ArrayList<Video> videoList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(getJSon(url));
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

    public ArrayList<Category> getCategoryList(String url) {

        ArrayList<Category> categoryList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(getJSon(url));
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonCategory = jsonArray.getJSONObject(i);
                String title = jsonCategory.getString("title");
                String urlCategoryThumb = jsonCategory.getString("thumb");
                categoryList.add(new Category(urlCategoryThumb, title));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return categoryList;
    }
}