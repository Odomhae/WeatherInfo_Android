package com.odom.briefweatherinfo.util

import retrofit2.converter.gson.GsonConverterFactory

import retrofit2.Retrofit

class CurrentWeatherClient {

    private val baseUrl = "https://api.openweathermap.org"
    private val mGetApi: CurrentWeatherApi
    val api: CurrentWeatherApi
        get() = mGetApi


    init {
        val mRetrofit: Retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build() // Retrofit 객체 생성

        mGetApi = mRetrofit.create(CurrentWeatherApi::class.java)
    }
}