package com.fake.autotaskapp

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import com.fake.autotaskapp.data.WifiItem

class AutoAccessibilityService : AccessibilityService() {

    companion object {
        private var accessibilityService: AutoAccessibilityService? = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }


    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = serviceInfo
        info.packageNames = null
        accessibilityService = this

        Log.d("qqq", "Service connected")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        stopSelf()
        return super.onUnbind(intent)
    }

    fun runningTask() {
        waitForNodeAndClick("Check-In", timeout = 20000, isId = false)
//        waitForNodeAndClick(
//            "com.dapp.metablox:id/rtv_event",
//            timeout = 20000
//        )
        Thread.sleep(1000)
        if (waitForNodeExist("com.dapp.metablox:id/rtv_check_in")) { // Nếu có nút Check-In 2
            Log.d("qqq", "runningTask: if")
            waitForNodeAndClick("com.dapp.metablox:id/rtv_check_in")
            waitForNodeAndClick("com.dapp.metablox:id/rtv_sign")
            Thread.sleep(3000)
            waitForNodeAndClick("com.dapp.metablox:id/rtv_got_it", timeout = 20000)
            Thread.sleep(3000)
            waitForNodeAndClick("com.dapp.metablox:id/img_point")
            Thread.sleep(3000)
            waitForNodeAndClick("com.dapp.metablox:id/rtv_done")
            Thread.sleep(3000)
            waitForNodeAndClick("com.dapp.metablox:id/rtv_got_it")
            Thread.sleep(3000)
            waitForNodeAndClick("Done", isId = false)
            Thread.sleep(1000)
            accessibilityService?.performGlobalAction(GLOBAL_ACTION_BACK)
            accessibilityService?.performGlobalAction(GLOBAL_ACTION_BACK)
            accessibilityService?.performGlobalAction(GLOBAL_ACTION_BACK)
            accessibilityService?.performGlobalAction(GLOBAL_ACTION_BACK)
            Thread.sleep(5000)
            // Xong hết thì chạy lại từ đầu bằng việc đổi wifi khác
        } else if (waitForNodeExist("Next Check-In:", isId = false)) {
            // Nếu có Cool down thì chạy lại từ đầu bằng việc đổi wifi khác
            Log.d("qqq", "runningTask: done")
            Thread.sleep(1000)
            accessibilityService?.performGlobalAction(GLOBAL_ACTION_BACK)
            accessibilityService?.performGlobalAction(GLOBAL_ACTION_BACK)
            accessibilityService?.performGlobalAction(GLOBAL_ACTION_BACK)
            accessibilityService?.performGlobalAction(GLOBAL_ACTION_BACK)
            accessibilityService?.performGlobalAction(GLOBAL_ACTION_BACK)
            Thread.sleep(5000)
        }
    }

    @SuppressLint("MissingPermission")
    fun autoRunCheckinWithWifiList(
        wifiList: List<WifiItem>,
        packageName: String = "com.dapp.metablox"
    ) {
        Thread {
            val context = accessibilityService?.baseContext ?: return@Thread
            for (wifi in wifiList) {
                context.handlerToast("Kết nối Wi-Fi: ${wifi.ssid}")
                WifiConnector.connectToSavedWifi(context, wifi.ssid)
                Thread.sleep(1000)
                Log.d("qqq", "🕐 kết nối xong...")
                //mo app
                WifiHelper.openApp(context, packageName)
//                WifiHelper.openApp(context, "dkapp.vaq.jpn")
                Thread.sleep(10000)
                runningTask()
                context.handlerToast("Xong toàn bộ")
            }
            context.handlerToast("Xong toàn bộ")
        }.start()
    }

    @SuppressLint("MissingPermission")
    fun runAllPackage(
        wifiList: List<WifiItem>,
        packageList: List<String>
    ) {
        Thread {
            val context = accessibilityService?.baseContext ?: return@Thread
            for (pkgName in packageList) {
                for (wifi in wifiList) {
                    context.handlerToast("Kết nối Wi-Fi: ${wifi.ssid}")
                    WifiConnector.connectToSavedWifi(context, wifi.ssid)
                    Thread.sleep(3000)
                    context.handlerToast("Kết nối Ok Wi-Fi: ${wifi.ssid}")
                    WifiHelper.openApp(context, pkgName)
                    Thread.sleep(10000)
                    runningTask()
                    context.handlerToast("Xong: ${wifi.ssid}")
                    Thread.sleep(2000)
                }
                context.handlerToast("Xong: ${pkgName}")
                //xong 1 app
                Thread.sleep(3000)
                accessibilityService?.performGlobalAction(GLOBAL_ACTION_BACK)
                accessibilityService?.performGlobalAction(GLOBAL_ACTION_BACK)
                accessibilityService?.performGlobalAction(GLOBAL_ACTION_BACK)
                Thread.sleep(2000)
            }
            context.handlerToast("Xong toàn bộ")
            Thread.sleep(3000)
            accessibilityService?.performGlobalAction(GLOBAL_ACTION_BACK)
            accessibilityService?.performGlobalAction(GLOBAL_ACTION_BACK)
            accessibilityService?.performGlobalAction(GLOBAL_ACTION_BACK)
        }.start()
    }

    fun autoSSID(wifi: WifiItem) {
        val context = accessibilityService?.baseContext ?: return
        context.handlerToast("Kết nối Wi-Fi: ${wifi.ssid}")
        WifiConnector.connectToSavedWifi(context, wifi.ssid)
        //mo app
//        WifiHelper.openApp(context, "com.dapp.metablox")
        Thread.sleep(10000)
        Thread {
            Thread.sleep(5000)
            Log.d("qqq", "🕐 kết nối xong...")
            runningTask()
            context.handlerToast("Xong toàn bộ")
        }.start()
    }

    fun testConnectWifi(wifi: WifiItem) {
        val context = accessibilityService?.baseContext ?: return
        WifiConnector.connectToSavedWifi(context, wifi.ssid)
        context.handlerToast("Kết nối Wi-Fi: ${wifi.ssid}")
    }

    private fun waitForNodeAndClick(
        viewId: String,
        timeout: Long = 10000L,
        interval: Long = 1000,
        isId: Boolean = true
    ): Boolean {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeout) {
            val root = accessibilityService?.rootInActiveWindow
            val node = if (isId) {
                root?.findAccessibilityNodeInfosByViewId(viewId)?.firstOrNull()
            } else {
                root?.findAccessibilityNodeInfosByText(viewId)?.firstOrNull()
            }
            if (node != null) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d("qqq", "✅ Clicked $viewId")
                return true
            }
            Thread.sleep(interval)
        }
        Log.w("qqq", "⏰ Timeout for $viewId")
        return false
    }

    private fun waitForNodeExist(
        viewId: String,
        timeoutMs: Long = 8000L,
        intervalMs: Long = 500L,
        isId: Boolean = true
    ): Boolean {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            val root = accessibilityService?.rootInActiveWindow
            val node = if (isId) {
                root?.findAccessibilityNodeInfosByViewId(viewId)?.firstOrNull()
            } else {
                root?.findAccessibilityNodeInfosByText(viewId)?.firstOrNull()
            }
            if (node != null) {
                return true
            }
            Thread.sleep(intervalMs)
        }
        return false
    }
}


fun Context.handlerToast(message: String) {
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

//            Thread.sleep(3000)
//            waitForNodeAndClick("com.dapp.metablox:id/img_esim")


