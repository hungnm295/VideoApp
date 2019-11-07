package com.example.videoapp.model.retrofit;

import com.example.videoapp.utilities.Constant;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HttpHelper {
    //Cai nay la 1 mẫu thiết kế (Design Pattern) có tên là Singleton
    private static HttpHelper INSTANCE;
    public synchronized static HttpHelper getInstance() {
        // Nếu INSTANCE == null thì khởi tạo
        // != null -> return ra kết quả luôn
        // Class này chỉ khởi tạo 1 lần duy nhất (Tránh khởi tạo nhiều lần)
        if(INSTANCE == null) {
            INSTANCE = new HttpHelper();
        }
        return INSTANCE;
    }

    private RequestAPI requestAPI;
    public HttpHelper() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.level(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constant.HOTVIDEO_BASEURL) //CÁi baseURL +
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        requestAPI = retrofit.create(RequestAPI.class);
    }

    public RequestAPI getRequestAPI() {
        return requestAPI;
    }

    //Tại sao cần có synchronized ạ
    // Để đảm bảo đồng bộ giữa các Thread. Tránh INSTANCE chưa được khởi tạo xong
    //Mà lại có thằng khác gọi getInstance tiếp -->  nó cheeck INSTANCE == null thì khởi tạo
    //--> Dẫn đến có 2 INSTANCE tồn tại 1 lúc

}
