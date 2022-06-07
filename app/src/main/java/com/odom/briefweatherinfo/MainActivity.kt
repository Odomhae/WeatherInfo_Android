package com.odom.briefweatherinfo

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ItemTouchHelper
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
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.odom.briefweatherinfo.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    var mRealm : Realm? = null
    var retrofitInterface= CurrentWeatherClient().mGetApi

    private lateinit var rvAdapter: WeatherRvAdapter
    private lateinit var weatherList : ArrayList<LocationRealmObject>
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.activity = this

        binding.swipeRefreshLayout.setOnRefreshListener {
            onResume()
            binding.swipeRefreshLayout.isRefreshing = false

        }

        mRealm = Realm.getDefaultInstance()
    }

    override fun onResume() {
        super.onResume()
        jobGetTotalInfo().start()

        weatherList = loadDbData()
        if(weatherList.isNotEmpty()){
            rvAdapter = WeatherRvAdapter(weatherList, mRealm)
            rv_weather.apply {
                adapter = WeatherRvAdapter(weatherList, mRealm)
                layoutManager = LinearLayoutManager(context)
            }

            for (realmObject in weatherList) {
                // Log.d("===realm data", realmObject.name + "// "+  realmObject.lat + "// "+  realmObject.lng)
                getLocationWeather(realmObject.lat, realmObject.lng)
            }
        }

        setItemTouchHelper()
    }

    fun setItemTouchHelper(){

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
            ItemTouchHelper.UP or ItemTouchHelper.DOWN){

            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                val dragFlags: Int  = ItemTouchHelper.START or ItemTouchHelper.END // drag disable
                val swipeFlags :Int = ItemTouchHelper.START or ItemTouchHelper.END
                return ItemTouchHelper.Callback.makeMovementFlags(dragFlags, swipeFlags)
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                //위치 swap
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                (rv_weather.adapter as WeatherRvAdapter).deleteList(viewHolder.adapterPosition)
            }

        }).apply {
            attachToRecyclerView(rv_weather)
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

                        val body = response.body()
                        val b = body?.clouds?.all
                        val currentTemp = body?.main?.temp

                        Log.d("===yy", b.toString() + "  / " + currentTemp)

                        //update temp
                        mRealm?.beginTransaction()
                        val info = mRealm?.where(LocationRealmObject::class.java)?.equalTo("lat" , lat)?.findFirst()
                        info?.currentTemp = currentTemp!!
                        mRealm?.commitTransaction()
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

    // convert RealmResults to ArrayList
    private fun loadDbData() : ArrayList<LocationRealmObject>{
        val realmResults: RealmResults<LocationRealmObject>? = mRealm?.where(LocationRealmObject::class.java)?.findAll()
        val myList = ArrayList<LocationRealmObject>()
        myList.addAll(mRealm!!.copyFromRealm(realmResults))

        return myList
    }

    fun settingBtnListener(view : View){
        val intent = Intent(this, SettingActivity::class.java)
        startActivity(intent)
    }

    fun addBtnListener(view : View){
        val intent = Intent(this, AddLocationActivity::class.java)
        startActivity(intent)
    }

}