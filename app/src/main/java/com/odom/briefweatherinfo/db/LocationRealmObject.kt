package com.odom.briefweatherinfo.db

import io.realm.RealmObject

open class LocationRealmObject : RealmObject() {

    var name : String? = null
    var lat = 0.0
    var lng = 0.0
    var currentTemp = 0.0
}