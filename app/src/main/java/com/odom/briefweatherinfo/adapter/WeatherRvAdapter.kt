package com.odom.briefweatherinfo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.odom.briefweatherinfo.R
import com.odom.briefweatherinfo.db.LocationRealmObject
import io.realm.RealmResults

class WeatherRvAdapter(private var weatherInfo : RealmResults<LocationRealmObject>):
    RecyclerView.Adapter<WeatherRvAdapter.WeatherViewHolder>() {

    class WeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val city : TextView = itemView.findViewById(R.id.item_cityName)
        val lat : TextView = itemView.findViewById(R.id.item_cityLat)
        val lng : TextView = itemView.findViewById(R.id.item_cityLng)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_weather, parent, false)
        return WeatherViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        holder.city.text = weatherInfo[position]?.name
        holder.lat.text = weatherInfo[position]?.lat.toString()
        holder.lng.text = weatherInfo[position]?.lng.toString()
    }

    override fun getItemCount() = weatherInfo.size
}