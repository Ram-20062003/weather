package com.example.weatherreport;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface JsonHolder {
    @GET("weather")
    Call<Weather_Class> get_weather(@Query("lat") String lat,@Query("lon") String lon,@Query("appid") String appid);

}
