package com.odom.briefweatherinfo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.odom.briefweatherinfo.databinding.RvItemWeatherBinding
import com.odom.briefweatherinfo.db.LocationRealmObject
import io.realm.Realm
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
}