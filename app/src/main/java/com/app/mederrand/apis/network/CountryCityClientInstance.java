package com.app.mederrand.apis.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class CountryCityClientInstance {
    private static Retrofit retrofit;
    public static final String COUNTRY_CITY_BASE_URL = "https://nominatim.openstreetmap.org/";

    public static Retrofit getCountryCityInstance() {
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(120, TimeUnit.SECONDS)
                .connectTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build();

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(COUNTRY_CITY_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create()) // Use RxJava 3 call adapter
                    .client(okHttpClient)
                    .build();
        }

        return retrofit;
    }
}
