package com.odom.briefweatherinfo.util

import com.odom.briefweatherinfo.model.CurrentWeatherResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface CurrentWeatherApi {

    @GET("/data/2.5/weather")
    fun getCurrentWeather(
        @Query("lat") lat: Double?,
        @Query("lon") lon: Double?,
        @Query("units") units: String?,
        @Query("appid") appId: String?,

    ): Call<CurrentWeatherResult>

}