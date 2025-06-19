package com.fake.autotaskapp

import android.content.Context
import android.content.Intent
import android.provider.Settings

object WifiHelper {

    fun connectToWifi(context: Context, ssid: String, password: String) {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
        // Tại đây AccessibilityService sẽ tự chọn đúng SSID
    }

    fun openApp(context: Context, packageName: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun closeApp(packageName: String) {
        val process = Runtime.getRuntime().exec("am force-stop $packageName")
        process.waitFor()
    }
}
