package com.odom.briefweatherinfo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.odom.briefweatherinfo.databinding.RvItemWeatherBinding
import com.odom.briefweatherinfo.db.LocationRealmObject
import io.realm.Realm
import java.util.*
import kotlin.collections.ArrayList

class WeatherRvAdapter(private var weatherInfo : ArrayList<LocationRealmObject>, var mRealm : Realm?):
    RecyclerView.Adapter<WeatherRvAdapter.WeatherViewHolder>() {

    class WeatherViewHolder(private val binding: RvItemWeatherBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(location :LocationRealmObject){
              binding.locationData = location
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val binding = RvItemWeatherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WeatherViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        holder.bind(weatherInfo[position])
    }

    override fun getItemCount() = weatherInfo.size

    fun deleteList(position: Int){
        // 삭제할 location의 lat
        val deleteLat = weatherInfo[position].lat

        weatherInfo.removeAt(position)
        notifyItemRemoved(position)

        // realm에서 지우고
        mRealm?.executeTransaction { realm ->
            val deleteLocation = realm.where(LocationRealmObject::class.java)
                .equalTo("lat", deleteLat)
                .findFirst()

            if (deleteLocation != null) {
                deleteLocation.deleteFromRealm()
                weatherInfo.remove(deleteLocation)
            }
        }

    }

    fun movelist(fromPosisition : Int, toPosition : Int) : Boolean{

        Collections.swap(weatherInfo, fromPosisition, toPosition)
        notifyItemMoved(fromPosisition, toPosition)

        //realm에서도 변경
        val fromDataName = weatherInfo[fromPosisition].name
        val toDataName = weatherInfo[toPosition].name

        mRealm?.executeTransaction {
            val fromData = it.where(LocationRealmObject::class.java)
                .equalTo("name", fromDataName)
                .findFirst()

            val toData = it.where(LocationRealmObject::class.java)
                .equalTo("name", toDataName)
                .findFirst()

            if (fromData != null && toData != null) {
                fromData.currentTemp = weatherInfo[toPosition].currentTemp
                fromData.lat = weatherInfo[toPosition].lat
                fromData.lng = weatherInfo[toPosition].lng
                fromData.name = weatherInfo[toPosition].name

                toData.currentTemp = weatherInfo[fromPosisition].currentTemp
                toData.lat = weatherInfo[fromPosisition].lat
                toData.lng = weatherInfo[fromPosisition].lng
                toData.name = weatherInfo[fromPosisition].name
            }
        }

        return true
    }
}