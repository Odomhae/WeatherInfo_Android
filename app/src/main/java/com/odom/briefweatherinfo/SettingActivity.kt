package com.odom.briefweatherinfo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat

class SettingActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

}

class PreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference, rootKey)

        val unitPreference : ListPreference? = findPreference("temp_unit")
        val powerPreference : SwitchPreferenceCompat? = findPreference("push_onoff")
        val intervalPreference : ListPreference? = findPreference("push_interval")

        unitPreference?.setOnPreferenceChangeListener { _, newValue ->

            val index = unitPreference.findIndexOfValue(newValue.toString())
            unitPreference.summary = unitPreference.entries[index]

            true
        }

        powerPreference?.setOnPreferenceClickListener {
            when{
                powerPreference.isChecked -> {
                    intervalPreference?.isEnabled = true
                }

                else ->{
                    intervalPreference?.isEnabled = false
                }
            }

            true
        }

        intervalPreference?.isEnabled = (powerPreference?.isChecked == true)
//        intervalPreference?.setOnPreferenceChangeListener { _, newValue ->
//
//            val index = intervalPreference.findIndexOfValue(newValue.toString())
//            intervalPreference.summary = intervalPreference.entries[index]
//
//            true
//        }


    }
}