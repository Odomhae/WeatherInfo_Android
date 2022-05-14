package com.odom.briefweatherinfo

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.odom.briefweatherinfo.adapter.WeatherRvAdapter
import io.realm.Realm
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import com.odom.briefweatherinfo.db.LocationRealmObject


import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    var noti_text : String ? = null
    var noti_date_text  : String ? = null

    var mRealm : Realm? = null

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
            TotalInfoTask().execute()

            swipe_refresh_layout.isRefreshing = false
        }
    }

    override fun onResume() {
        super.onResume()
        TotalInfoTask().execute()

        val weatherList = loadDbData()
        if(weatherList != null){
            rv_weather.adapter = WeatherRvAdapter(weatherList)
            rv_weather.layoutManager = LinearLayoutManager(this)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        mRealm?.close()
    }

    fun readData(numOfRows:Int, pageNo:Int) : JSONObject {
        var url = URL(
            "http://apis.data.go.kr/1360000/VilageFcstMsgService/getWthrSituation?serviceKey=" +
                    "${BuildConfig.API_KEY}&numOfRows=${numOfRows}&pageNo=${pageNo}&stnId=108&dataType=JSON"
        )

        val connection = url.openConnection()
        val data = connection.getInputStream().readBytes().toString(charset("UTF-8"))

        return JSONObject(data)
    }

    @SuppressLint("StaticFieldLeak")
    inner class TotalInfoTask : AsyncTask<Void, JSONArray, String>() {

        val asyncDialog : ProgressDialog = ProgressDialog(this@MainActivity)

        override fun onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.BUTTON_POSITIVE)
            asyncDialog.setMessage("Loading...")
            asyncDialog.show()
        }

        override fun doInBackground(vararg params: Void?): String {

            val jsonObject = readData(10, 1)

            val items = jsonObject
                .getJSONObject("response")
                .getJSONObject("body")
                .getJSONObject("items")

            val itemArray = items.getJSONArray("item")
           // Log.d("===itemArray" , itemArray.toString())

            val item = itemArray.getJSONObject(0)
           // Log.d("===itemArray" , item.toString())

            result_notification.text = item.getString("wfSv1")
            result_notification_date.text = item.getString("tmFc")

            return "complete"
        }

        override fun onPostExecute(result: String?) {

            // ProgressDialog 종료
            asyncDialog.dismiss()
        }
    }

    private fun loadDbData() : RealmResults<LocationRealmObject>?{
        val realmResults: RealmResults<LocationRealmObject>? = mRealm?.where(LocationRealmObject::class.java)?.findAll()

        if (realmResults != null) {
            for (realmObject in realmResults) {
                Log.d("===realm data", realmObject.name + "// "+  realmObject.lat)

            }
        }

        return realmResults
    }
}