package com.odom.briefweatherinfo

import io.realm.RealmConfiguration

import android.app.Application
import io.realm.Realm


class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)

        val config = RealmConfiguration.Builder().build()
        Realm.setDefaultConfiguration(config)
    }
}