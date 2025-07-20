package com.odom.briefweatherinfo.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


object GeoUtil {

    fun getLocationFromName(context: Context, city: String, listener: GeoUtilListener) {

        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses: List<Address>

        try {
            addresses = geocoder.getFromLocationName(city, 1) as List<Address>
            if(addresses.size > 0){
                val lat: Double = addresses[0].latitude
                val lng: Double = addresses[0].longitude
                listener.onSuccess(city, lat, lng)

            }else{
                listener.onError("주소 결과가 없습니다")
            }

        } catch (e: IOException) {
            listener.onError(e.message)
        }
    }

    interface GeoUtilListener {
        fun onSuccess(city: String, lat: Double, lng: Double)
        fun onError(message: String?)
    }
}