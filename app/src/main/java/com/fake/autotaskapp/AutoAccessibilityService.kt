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
        waitForNodeAndClick(
            "com.dapp.metablox:id/rtv_event",
            timeout = 20000
        ) // Đợi app mở bấm nút Check-In
        Thread.sleep(1000)
        if (waitForNodeExist("com.dapp.metablox:id/rtv_check_in")) { // Nếu có nút Check-In 2
            Log.d("qqq", "runningTask: if")
            waitForNodeAndClick("com.dapp.metablox:id/rtv_check_in")
            waitForNodeAndClick("com.dapp.metablox:id/rtv_sign")
            waitForNodeAndClick("com.dapp.metablox:id/rtv_got_it", timeout = 20000)
            waitForNodeAndClick("com.dapp.metablox:id/img_point")
            waitForNodeAndClick("com.dapp.metablox:id/rtv_done")
            waitForNodeAndClick("com.dapp.metablox:id/rtv_got_it")
            waitForNodeAndClick("Done", isId = false)
            Thread.sleep(1000)
            accessibilityService?.performGlobalAction(GLOBAL_ACTION_BACK)
            Thread.sleep(20000)
            // Xong hết thì chạy lại từ đầu bằng việc đổi wifi khác
        } else if (waitForNodeExist("Next Check-In:", isId = false)) {
            // Nếu có Cool down thì chạy lại từ đầu bằng việc đổi wifi khác
            Log.d("qqq", "runningTask: done")
            Thread.sleep(1000)
            accessibilityService?.performGlobalAction(GLOBAL_ACTION_BACK)
        }
    }

    @SuppressLint("MissingPermission")
    fun autoRunCheckinWithWifiList() {
        val wifiList = listOf(
            WifiItem("Pham Ki", "mottoitam"),
            WifiItem("Kho", "mottoitam")
        )
        val context = accessibilityService?.baseContext ?: return
        WifiHelper.openApp(context, "com.dapp.metablox")

        Thread {
            for (wifi in wifiList) {
                context.handlerToast("Kết nối Wi-Fi: ${wifi.ssid}")
                AppEvent.updateText("Kết nối Wi-Fi: ${wifi.ssid}")

                WifiConnector.connectToSavedWifi(context, wifi.ssid)
                Thread.sleep(5000)
                Log.d("qqq", "🕐 kết nối xong...")
                runningTask()
                if (wifi != wifiList.last()) {
                    context.handlerToast("✅ Xong với ${wifi.ssid}, tiếp tục wifi tiếp theo")
                    AppEvent.updateText("Xong với ${wifi.ssid}, tiếp tục wifi tiếp theo")
                }
            }
            AppEvent.updateText("Xong toàn bộ")
            context.handlerToast("Xong toàn bộ")
        }.start()
    }

    fun autoSSID(wifi: WifiItem) {
        val context = accessibilityService?.baseContext ?: return
        WifiHelper.openApp(context, "com.dapp.metablox")
        Thread.sleep(10000)
        Thread {
            context.handlerToast("Kết nối Wi-Fi: ${wifi.ssid}")
            WifiConnector.connectToSavedWifi(context, wifi.ssid)
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