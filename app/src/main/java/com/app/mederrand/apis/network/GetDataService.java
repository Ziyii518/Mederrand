package com.app.mederrand.apis.network;


import com.app.mederrand.apis.models.auth.CountryCityResponseModel;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GetDataService {



    // GET COUNTRIES AND CITY NAME FROM LAT LON //
    @GET("reverse")
    Call<CountryCityResponseModel> getCountryCityName(@Query("format") String format,
                                                      @Query("lat") String lat,
                                                      @Query("lon") String lon,
                                                      @Query("zoom") String zoom,
                                                      @Query("addressdetails") String addressdetails
    );

}
