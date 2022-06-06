package com.odom.briefweatherinfo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.odom.briefweatherinfo.R
import com.odom.briefweatherinfo.db.LocationRealmObject
import io.realm.Realm
import kotlin.collections.ArrayList

class WeatherRvAdapter(private var weatherInfo : ArrayList<LocationRealmObject>, var mRealm : Realm?):
    RecyclerView.Adapter<WeatherRvAdapter.WeatherViewHolder>() {

    class WeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val city : TextView = itemView.findViewById(R.id.item_cityName)
        val lat : TextView = itemView.findViewById(R.id.item_cityLat)
        val lng : TextView = itemView.findViewById(R.id.item_cityLng)
        val currentTemp : TextView = itemView.findViewById(R.id.item_currentTemp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_item_weather, parent, false)
        return WeatherViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        holder.city.text = weatherInfo[position].name
        holder.lat.text = weatherInfo[position].lat.toString()
        holder.lng.text = weatherInfo[position].lng.toString()
        holder.currentTemp.text = weatherInfo[position].currentTemp.toString()
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