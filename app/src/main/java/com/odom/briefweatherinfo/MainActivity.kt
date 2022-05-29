package com.odom.briefweatherinfo

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.odom.briefweatherinfo.adapter.WeatherRvAdapter
import io.realm.Realm
import org.json.JSONObject
import java.net.URL
import com.odom.briefweatherinfo.db.LocationRealmObject
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.odom.briefweatherinfo.model.CurrentWeatherResult
import com.odom.briefweatherinfo.util.CurrentWeatherClient
import retrofit2.Call
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    var mRealm : Realm? = null
    var retrofitInterface= CurrentWeatherClient().mGetApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ib_setting!!.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }

        ib_add_location!!.setOnClickListener {
            val intent = Intent(this, AddLocationActivity::class.java)
            startActivity(intent)
        }

        mRealm = Realm.getDefaultInstance()

        // 당겨서 새로고침
        swipe_refresh_layout.setOnRefreshListener {
            onResume()

            swipe_refresh_layout.isRefreshing = false
        }
    }

    override fun onResume() {
        super.onResume()
        jobGetTotalInfo().start()

        val weatherList = loadDbData()
        if(weatherList != null){
            rv_weather.adapter = WeatherRvAdapter(weatherList)
            rv_weather.layoutManager = LinearLayoutManager(this)

            for (realmObject in weatherList) {
                Log.d("===realm data", realmObject.name + "// "+  realmObject.lat + "// "+  realmObject.lng)
                getLocationWeather(realmObject.lat, realmObject.lng)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        mRealm?.close()
    }

    private fun getLocationWeather(lat: Double, lng: Double){

        retrofitInterface.getCurrentWeather(lat, lng, "metric", BuildConfig.API_KEY2)
            .enqueue(object : retrofit2.Callback<CurrentWeatherResult> {

                override fun onResponse(call: Call<CurrentWeatherResult>, response: Response<CurrentWeatherResult>) {
                    if(response.isSuccessful){
                        Log.d("===onResponse",  "success")

                        val a = response.body()
                        val b = a?.clouds?.all
                        val c = a?.main?.temp

                        Log.d("===yy", b.toString() + "  / " + c )
                    }
                }

                override fun onFailure(call: Call<CurrentWeatherResult>, t: Throwable) {
                    Log.d("===onFailure",  "failure")
                }

            })
    }

    private fun readData() : JSONObject {
        val url = URL(
            "http://apis.data.go.kr/1360000/VilageFcstMsgService/getWthrSituation?serviceKey=" +
                    "${BuildConfig.API_KEY}&numOfRows=10&pageNo=1&stnId=108&dataType=JSON"
        )

        val connection = url.openConnection()
        val data = connection.getInputStream().readBytes().toString(charset("UTF-8"))

        return JSONObject(data)
    }

    // 전국 날씨정보 불러오기
    private fun jobGetTotalInfo() = CoroutineScope(Dispatchers.Main).launch {

        val asyncDialog = ProgressDialog(this@MainActivity)
        asyncDialog.setProgressStyle(ProgressDialog.BUTTON_POSITIVE)
        asyncDialog.setMessage("Loading...")
        asyncDialog.show()

        var totalWeatherInfo = ""
        var totalWeatherDate = ""

        withContext(Dispatchers.IO){
            try {

                val jsonObject = readData()

                val items = jsonObject
                    .getJSONObject("response")
                    .getJSONObject("body")
                    .getJSONObject("items")

                val itemArray = items.getJSONArray("item")
               //  Log.d("===itemArray" , itemArray.toString())

                val item = itemArray.getJSONObject(0)
               //  Log.d("===item" , item.toString())

                totalWeatherInfo = item.getString("wfSv1")
                totalWeatherDate = item.getString("tmFc")

            }catch (e: Exception){
                e.printStackTrace()
            }
        }

        result_notification.text = totalWeatherInfo
        result_notification_date.text = totalWeatherDate

        asyncDialog.dismiss()
    }

    private fun loadDbData() : RealmResults<LocationRealmObject>?{
        val realmResults: RealmResults<LocationRealmObject>? = mRealm?.where(LocationRealmObject::class.java)?.findAll()

//        if (realmResults != null) {
//            for (realmObject in realmResults) {
//                Log.d("===realm data", realmObject.name + "// "+  realmObject.lat)
//            }
//        }

        return realmResults
    }
}