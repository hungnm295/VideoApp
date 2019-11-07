package com.example.videoapp.model.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.videoapp.utilities.Constant;
import com.example.videoapp.model.object.Video;

import java.util.ArrayList;

public class SQLiteVideo extends SQLiteOpenHelper {

    SQLiteDatabase database;
    ContentValues contentValues;
    Cursor cursor;


    public SQLiteVideo(@Nullable Context context) {
        super(context, Constant.DB_NAME, null, Constant.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String queryTable = "CREATE TABLE Video( " +
                "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                "idVideo Text, " +
                "avatarURL Text, " +
                "videoURL Text, " +
                "title Text, " +
                "duration Text, " +
                "datePublish Text )";
        db.execSQL(queryTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + Constant.DB_TABLE_NAME);
        }
    }

    public void insertVideo(Video video) {
        database = getWritableDatabase();
        contentValues = new ContentValues();
        contentValues.put("idVideo", video.getId());
        contentValues.put("avatarURL", video.getAvatar());
        contentValues.put("videoURL", video.getFile_mp4());
        contentValues.put("title", video.getTitle());
        contentValues.put("duration", video.getDuration());
        contentValues.put("datePublish", video.getDate_published());
        database.insert(Constant.DB_TABLE_NAME, null, contentValues);
        closeDB();
    }

    public ArrayList<Video> getAllVideo() {
        ArrayList<Video> videoList = new ArrayList<>();
        database = getReadableDatabase();
        cursor = database.query(Constant.DB_TABLE_NAME, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String idVideo = cursor.getString(cursor.getColumnIndex("idVideo"));
            String avatarURL = cursor.getString(cursor.getColumnIndex("avatarURL"));
            String videoURL = cursor.getString(cursor.getColumnIndex("videoURL"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String duration = cursor.getString(cursor.getColumnIndex("duration"));
            String datePublish = cursor.getString(cursor.getColumnIndex("datePublish"));
            videoList.add(new Video(idVideo, avatarURL, videoURL, title, duration, datePublish));
        }
        return videoList;
    }

    public int deleteVideo(String id) {
        database = getWritableDatabase();
        database.delete(Constant.DB_TABLE_NAME, "idVideo=?", new String[]{id});
        return 1;
    }

    public boolean deleteAll() {
        database = getWritableDatabase();
        database.delete(Constant.DB_TABLE_NAME, null, null);
        return true;
    }

    private void closeDB() {
        if (database != null) database.close();
        if (contentValues != null) contentValues.clear();
        if (cursor != null) cursor.close();
    }

}
