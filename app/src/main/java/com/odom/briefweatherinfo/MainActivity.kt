package com.odom.briefweatherinfo

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
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
import com.odom.briefweatherinfo.databinding.ActivityMainBinding
import java.util.*


class MainActivity : AppCompatActivity() {

    var mRealm : Realm? = null
    var retrofitInterface= CurrentWeatherClient().mGetApi

    private lateinit var rvAdapter: WeatherRvAdapter
    private lateinit var weatherList : ArrayList<LocationRealmObject>
    private lateinit var binding: ActivityMainBinding
    
    var isCollapsed = true

    // 뒤로가기 2번 종료
    var backPressTime = 0

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // 뒤로가기 클릭 시 실행시킬 코드 입력
            if (System.currentTimeMillis().toInt() - backPressTime > 2000){
                backPressTime = System.currentTimeMillis().toInt()
                Toast.makeText(this@MainActivity, "한번 더 누르면 앱이 종료됩니다." , Toast.LENGTH_SHORT).show()
            } else {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.activity = this

        binding.swipeRefreshLayout.setOnRefreshListener {
            onResume()
            binding.swipeRefreshLayout.isRefreshing = false

        }

        mRealm = Realm.getDefaultInstance()
        this.onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onResume() {
        super.onResume()
        jobGetTotalInfo().start()

        weatherList = loadDbData()

        for (realmObject in weatherList) {
            // Log.d("===realm data", realmObject.name + "// "+  realmObject.lat + "// "+  realmObject.lng)
            getLocationWeather(realmObject.lat, realmObject.lng)
        }

        weatherList = loadDbData()

        if(weatherList.isNotEmpty()){
            rvAdapter = WeatherRvAdapter(weatherList, mRealm)
            rv_weather.apply {
                adapter = WeatherRvAdapter(weatherList, mRealm)
                layoutManager = LinearLayoutManager(context)
            }

        }

        setItemTouchHelper()
    }

    fun setItemTouchHelper(){

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
            ItemTouchHelper.UP or ItemTouchHelper.DOWN){

            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                val dragFlags: Int  = ItemTouchHelper.UP or ItemTouchHelper.DOWN // drag disable
                val swipeFlags :Int = ItemTouchHelper.START or ItemTouchHelper.END
                return ItemTouchHelper.Callback.makeMovementFlags(0, swipeFlags)
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                //위치 swap
              //  (rv_weather.adapter as WeatherRvAdapter).movelist(viewHolder.adapterPosition, target.adapterPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                (rv_weather.adapter as WeatherRvAdapter).deleteList(viewHolder.adapterPosition)
            }

        }).apply {
            attachToRecyclerView(rv_weather)
        }
    }

    override fun onBackPressed() {
        if (System.currentTimeMillis().toInt() - backPressTime > 2000){
            backPressTime = System.currentTimeMillis().toInt()
            Toast.makeText(this, "한번 더 누르면 앱이 종료됩니다." , Toast.LENGTH_SHORT).show()
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mRealm?.close()
    }

    private fun getLocationWeather(lat: Double, lng: Double){

        retrofitInterface.getCurrentWeather(lat, lng, "metric", BuildConfig.API_KEY2, "kr")
            .enqueue(object : retrofit2.Callback<CurrentWeatherResult> {

                override fun onResponse(call: Call<CurrentWeatherResult>, response: Response<CurrentWeatherResult>) {
                    if(response.isSuccessful){
                        Log.d("===onResponse",  "success")

                        val body = response.body()
                        val weather = body?.weather?.get(0)
                        val currentTemp = body?.main?.temp
                        val weatherMain = weather?.main

                        Log.d("===yy", weatherMain + "  / " +weather?.description +" /" + weather?.id + "  / " + currentTemp)
                        // D/===yy: Clear  / 맑음 /800  / 27.95
                        // D/===yy: Sand  / 모래 /751  / 10.38

                        // https://openweathermap.org/weather-conditions

                        //update temp
                        mRealm?.beginTransaction()
                        val info = mRealm?.where(LocationRealmObject::class.java)?.equalTo("lat" , lat)?.findFirst()
                        info?.currentTemp = currentTemp!!
                        info?.main = weatherMain

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
                val rawDate = item.getString("tmFc")

                // 기준날짜
                val year = rawDate.substring(2 until 4)
                val month = rawDate.substring(4 until 6)
                val day = rawDate.substring(6 until 8)
                val time = rawDate.substring(8 until 10)
                // Log.d("===time", "$year/ $month/ $day/ $time")

                totalWeatherDate = "$month/$day $time:00 기준"

            }catch (e: Exception){
                e.printStackTrace()
            }
        }

        result_notification.text = totalWeatherInfo
        result_notification_date.text = totalWeatherDate

        result_notification.setOnClickListener {
            if (isCollapsed) {
                result_notification.maxLines = Int.MAX_VALUE
            } else {
                result_notification.maxLines = 4
            }
            isCollapsed = !isCollapsed
        }

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