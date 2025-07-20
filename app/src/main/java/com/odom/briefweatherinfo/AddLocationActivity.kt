package com.odom.briefweatherinfo

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.odom.briefweatherinfo.databinding.ActivityAddLocationBinding
import com.odom.briefweatherinfo.util.GeoUtil.GeoUtilListener
import com.odom.briefweatherinfo.util.GeoUtil.getLocationFromName
import com.odom.briefweatherinfo.db.LocationRealmObject

import io.realm.Realm


class AddLocationActivity : AppCompatActivity() {

    var mRealm : Realm? = null
    private lateinit var binding : ActivityAddLocationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_location)
        binding.activity = this

        binding.tvAutoComplete.setOnEditorActionListener { _, actionId, _ ->

            val city = binding.tvAutoComplete?.text.toString()
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                getLocation(city)
            }

            false
        }

        mRealm = Realm.getDefaultInstance()
    }

    override fun onDestroy() {
        super.onDestroy()
        mRealm?.close()
    }

    fun searchBtnListener(view : View){
        val city = binding.tvAutoComplete.text.toString()
        if(TextUtils.isEmpty(city)) return

        getLocation(city)
    }

    private fun getLocation(city : String){

        getLocationFromName(this, city, object : GeoUtilListener {
            override fun onSuccess(city: String, lat: Double, lng: Double) {

                Log.d("===검색성공", "$city, $lat, $lng")
                // 중복확인 후 db에 넣고
                saveNewCity(city, lat, lng)
                Toast.makeText(this@AddLocationActivity, "$city 추가 완료" , Toast.LENGTH_SHORT).show()

                // 종료
                finish()
            }

            override fun onError(message: String?) {
                Log.d("===검색실패", message.toString())
                Toast.makeText(this@AddLocationActivity, message, Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun saveNewCity(city: String, lat: Double, lng : Double){

        try {
            mRealm?.beginTransaction()
            val newLocationRealmObject: LocationRealmObject? = mRealm?.createObject(LocationRealmObject::class.java)

            if (newLocationRealmObject != null) {
                newLocationRealmObject.name = city
                newLocationRealmObject.lat = lat
                newLocationRealmObject.lng = lng
            }

            mRealm?.commitTransaction()

        }catch (e:Exception){
            e.printStackTrace()
        }

    }

}