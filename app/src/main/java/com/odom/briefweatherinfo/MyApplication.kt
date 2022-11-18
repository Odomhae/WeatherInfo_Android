package com.odom.briefweatherinfo

import io.realm.RealmConfiguration

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import io.realm.Realm


class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)

        val config = RealmConfiguration.Builder()
            .allowWritesOnUiThread(true)    // UI Thread에서도 realm에 접근할 수 있도록 한다.
            .deleteRealmIfMigrationNeeded() // realm 의 데이터를 바꿀 때 마다 Migration 을 설정해 주어야 되는데
                                            // 그럴 필요 없이 자동으로 기존의 realm 데이터를 삭제하여 앱이 동작하게 해줌
            .build()
        Realm.setDefaultConfiguration(config)

        initRemoteConfig()
    }

    private fun initRemoteConfig() {

        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        //todo
        val version = remoteConfig.getString("latest_version")
        //Log.d("===ss", version)

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                   // Log.d("===ss", "Fetch succeeded: $updated")
                } else {
                    //Log.d("===ss", "Fetch failed")
                }
            }

    }
}