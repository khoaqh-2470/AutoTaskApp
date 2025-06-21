package com.fake.autotaskapp.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit

object WifiStorage {
    private const val PREF_NAME = "wifi_prefs"
    private const val KEY_WIFI_LIST = "wifi_list"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveWifiList(context: Context, wifiList: List<WifiItem>) {
        val json = Gson().toJson(wifiList)
        getPrefs(context).edit { putString(KEY_WIFI_LIST, json) }
    }

    fun getWifiList(context: Context): List<WifiItem> {
        val json = getPrefs(context).getString(KEY_WIFI_LIST, null)
        return if (json != null) {
            val type = object : TypeToken<List<WifiItem>>() {}.type
            Gson().fromJson(json, type)
        } else {
            emptyList()
        }
    }
}

