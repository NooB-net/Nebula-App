package com.nebula.app.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    private static final String NASA_IMAGES_BASE_URL = "https://images-api.nasa.gov/";
    private static final String ISS_BASE_URL = "https://api.wheretheiss.at/";

    private static Retrofit nasaImagesRetrofit;
    private static Retrofit issRetrofit;

    private static OkHttpClient getClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public static synchronized NasaImagesApiService getNasaImagesApi() {
        if (nasaImagesRetrofit == null) {
            nasaImagesRetrofit = new Retrofit.Builder()
                    .baseUrl(NASA_IMAGES_BASE_URL)
                    .client(getClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return nasaImagesRetrofit.create(NasaImagesApiService.class);
    }

    public static synchronized IssApiService getIssApi() {
        if (issRetrofit == null) {
            issRetrofit = new Retrofit.Builder()
                    .baseUrl(ISS_BASE_URL)
                    .client(getClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return issRetrofit.create(IssApiService.class);
    }
}
