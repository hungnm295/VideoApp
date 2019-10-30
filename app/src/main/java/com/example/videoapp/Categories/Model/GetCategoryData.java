package com.example.videoapp.Categories.Model;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class GetCategoryData {
    private static GetCategoryData INSTANCE;
    public synchronized static GetCategoryData getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new GetCategoryData();
        }
        return INSTANCE;
    }
    public ArrayList<Category> getCategory(String urlCategory) {
        String result = "";
        ArrayList<Category> categoryList = new ArrayList<>();
        try {
            URL url = new URL(urlCategory);
            URLConnection connection = url.openConnection();
            InputStream in = connection.getInputStream();
            int byteCharacter;
            while ((byteCharacter = in.read()) != -1) {
                result += (char) byteCharacter;
            }
            JSONArray jsonArray = new JSONArray(result);
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
