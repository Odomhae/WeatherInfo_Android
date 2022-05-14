package com.odom.briefweatherinfo

import android.Manifest
import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.odom.briefweatherinfo.util.GeoUtil.GeoUtilListener
import com.odom.briefweatherinfo.util.GeoUtil.getLocationFromName
import com.odom.briefweatherinfo.db.LocationRealmObject

import io.realm.Realm
import kotlinx.android.synthetic.main.activity_add_location.*


class AddLocationActivity : AppCompatActivity() {

    var PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.INTERNET
    )
    val REQUEST_PERMISSION_CODE = 1
    val CITY_HALL = LatLng(37.566648, 126.978449)

    var btMylocation : Button? = null

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient //  implementation 'com.google.android.gms:play-services-location:19.0.1'

    var lastKnownLocation2 : Location?= null

    var mRealm : Realm? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_location)

//        btMylocation = findViewById(R.id.bt_get_my_location)
//        btMylocation?.setOnClickListener {
//            addCurrentLocation()
//        }

//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
//
//        //권한 요청
//        ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_CODE)

        tv_autoComplete?.setOnEditorActionListener { _, actionId, _ ->

            val city = tv_autoComplete?.text.toString()
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                getLocation(city)
            }

            false
        }

        iv_search?.setOnClickListener {
            val city = tv_autoComplete?.text.toString()
            if(TextUtils.isEmpty(city)){
                return@setOnClickListener
            }

            getLocation(city)
        }

        mRealm = Realm.getDefaultInstance()
    }

    override fun onDestroy() {
        super.onDestroy()
        mRealm?.close()
    }

    private fun getLocation(city : String){

        getLocationFromName(this, city, object : GeoUtilListener {
            override fun onSuccess(city: String, lat: Double, lng: Double) {

                Log.d("===검색성공", "$city, $lat, $lng")
                // 중복확인 후 db에 넣고
                saveNewCity(city, lat, lng)
                Toast.makeText(this@AddLocationActivity, "$city 추가 완료 : $lat, $lng" , Toast.LENGTH_SHORT).show()

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

//    private fun hasPermissions() : Boolean{
//        for(permisison in PERMISSIONS)
//            if(ActivityCompat.checkSelfPermission(this, permisison) != PackageManager.PERMISSION_GRANTED)
//                return false
//
//        return true
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        getCurrentLocation()
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun getCurrentLocation() : LatLng {
//
//        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        val locationProvider : String = LocationManager.GPS_PROVIDER
//        var lastKnownLocation : Location? = locationManager.getLastKnownLocation(locationProvider)
//
//        Log.d("===TAGgg", lastKnownLocation.toString())
//
//        if(lastKnownLocation == null){
//            Log.d("===TAG", "2222222")
//
//            fusedLocationProviderClient.lastLocation
//                .addOnSuccessListener { location ->
//                    if(location == null) {
//                        Log.d("===TAG", "location get fail")
//                        val mLocationCallback = object : LocationCallback(){
//                            override fun onLocationResult(locationResult: LocationResult) {
//                                super.onLocationResult(locationResult)
//                                if(/*locationResult != null  && */ locationResult.locations.isNotEmpty()){
//                                    lastKnownLocation = locationResult.locations[0]
//                                    Log.d("===TAG1", lastKnownLocation.toString())
//                                }else{
//                                    Log.d("===TAG1", "yyy")
//                                }
//                            }
//                        }
//
//                        // 최근 위치정보 업데이트
//                        val locationRequest = LocationRequest.create()
//                        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//                        Log.d("===TAG123", mLocationCallback.toString())
//                        fusedLocationProviderClient.requestLocationUpdates( locationRequest, mLocationCallback, Looper.getMainLooper())
//                        Log.d("===TAG12", lastKnownLocation.toString())
//
//                    } else {
//                        lastKnownLocation = location
//                        lastKnownLocation2 = location
//                        val myLoc = LatLng(location.latitude, location.longitude)
//                        Log.d("===TAG13", myLoc.toString() + "// " + lastKnownLocation.toString())
//                        Log.d("===TAG14", lastKnownLocation2.toString() )
//
//                    }
//                }
//
//        }else{
//            val myLoc = LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
//            Log.d("===TAG2", myLoc.toString())
//        }
//
//        if(lastKnownLocation == null){
//            Log.d("TAG", "위치 확인불가")
//            return LatLng(CITY_HALL.latitude, CITY_HALL.longitude)
//
//        }
//
//        // 경도, 위도 위치 반환
//        Log.d("TAG", lastKnownLocation!!.latitude.toString() + " // "+  lastKnownLocation!!.longitude.toString())
//        return LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
//    }
//
//    private fun addCurrentLocation(){
//
//        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        when{
//            hasPermissions() ->{
//                // 권한있는데 GPS 꺼져있으면 켜는 화면으로 이동
//                if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
//                    val builder = AlertDialog.Builder(this)
//                    builder.setTitle("GPS가 꺼져있습니다.")
//                        .setPositiveButton("확인") { _, _ ->
//                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//                            intent.addCategory(Intent.CATEGORY_DEFAULT)
//                            startActivity(intent)
//                        }
//                        .setNegativeButton("취소") {_, _ ->
//                        }
//
//                    val alertDialog = builder.create()
//                    alertDialog.show()
//
//                }else{
//                    // 현재 위치 추가
//                    // 중복여부 체크
//                    Log.d("===TAG", "권한있음"+" 위치 :"+ getCurrentLocation().toString())
//                    Log.d("===TAG권한있음", lastKnownLocation2.toString() )
//
//                    Toast.makeText(this, lastKnownLocation2?.latitude.toString() + " 추가됨", Toast.LENGTH_SHORT).show()
//                }
//
//            }
//
//            else -> {
//                val builder = AlertDialog.Builder(this)
//                builder.setTitle("위치 사용권한에 동의해주세요.")
//                    .setPositiveButton("확인") { _, _ ->
//                        //권한 요청
//                        ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_CODE)
//                    }
//                    .setNegativeButton("취소") {_, _ ->
//                        Toast.makeText(applicationContext, "위치 사용권한에 동의하지 않았습니다.", Toast.LENGTH_SHORT).show()
//                    }
//
//                val alertDialog = builder.create()
//                alertDialog.show()
//            }
//
//        }
//
//    }

}