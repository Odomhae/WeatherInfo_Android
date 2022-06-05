package com.odom.briefweatherinfo

import io.realm.RealmConfiguration

import android.app.Application
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
    }
}