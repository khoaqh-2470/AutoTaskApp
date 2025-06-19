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
import androidx.annotation.RequiresPermission
import com.fake.autotaskapp.data.WifiItem

class AutoAccessibilityService : AccessibilityService() {

    companion object {
        var currentSsid: String? = null
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
//        WifiHelper.openApp(accessibilityService?.baseContext ?: return, "com.dapp.metablox")
//        Thread.sleep(10000)
        waitForNodeAndClick(
            "com.dapp.metablox:id/rtv_event",
            timeout = 20000
        ) // Đợi app mở bấm nút Check-In
        Thread.sleep(1000)
        if (waitForNodeExist("com.dapp.metablox:id/rtv_check_in")) { // Nếu có nút Check-In 2
            Log.d("qqq", "runningTask: if")
            waitForNodeAndClick("com.dapp.metablox:id/rtv_check_in")
            Thread.sleep(3000)
            waitForNodeAndClick("com.dapp.metablox:id/rtv_sign")
            Thread.sleep(3000)
            waitForNodeAndClick("com.dapp.metablox:id/rtv_got_it")
            Thread.sleep(1000)
            accessibilityService?.performGlobalAction(GLOBAL_ACTION_BACK)
            Thread.sleep(20000)
            // Xong hết thì chạy lại từ đầu bằng việc đổi wifi khác
        } else if (waitForNodeExist("com.dapp.metablox:id/rll_cool_down")) { // Nếu có Cool down thì chạy lại từ đầu bằng việc đổi wifi khác
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
                Log.d("qqq", "👉 Kết nối Wi-Fi: ${wifi.ssid}")
                context.handlerToast("Kết nối Wi-Fi: ${wifi.ssid}")
//                val success = switchToSavedWifi(context, wifi.ssid)
                WifiConnector.connectToSavedWifi(context, wifi.ssid)
                Thread.sleep(5000)
                Log.d("qqq", "🕐 kết nối xong...")
                runningTask()

                Log.d("qqq", "✅ Xong với ${wifi.ssid}, tiếp tục wifi tiếp theo")
                if (wifi != wifiList.last()) {
                    context.handlerToast("✅ Xong với ${wifi.ssid}, tiếp tục wifi tiếp theo")
                }
            }
            context.handlerToast("Xong toàn bộ")
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                    connectWifiApi29(context, Pair(wifi.ssid, wifi.password))
//                } else {
//                    connectWifi(context, Pair(wifi.ssid, wifi.password))
//                }
        }.start()
    }

    private fun waitForNodeAndClick(
        viewId: String,
        timeout: Long = 8000L,
        interval: Long = 1000
    ): Boolean {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeout) {
            val root = accessibilityService?.rootInActiveWindow
            val node = root?.findAccessibilityNodeInfosByViewId(viewId)?.firstOrNull()
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

    fun waitForNodeExist(
        viewId: String,
        timeoutMs: Long = 8000L,
        intervalMs: Long = 500L
    ): Boolean {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            val root = accessibilityService?.rootInActiveWindow
            val node = root?.findAccessibilityNodeInfosByViewId(viewId)?.firstOrNull()
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

@RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
@SuppressLint("ServiceCast", "MissingPermission")
fun switchToSavedWifi(context: Context, ssid: String): Boolean {
    val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    // Tìm mạng đã lưu khớp với SSID
    val targetNetwork = wifiManager.configuredNetworks?.firstOrNull {
        it.SSID?.removeSurrounding("\"") == ssid
    }

    return if (targetNetwork != null) {
        wifiManager.disconnect()
        wifiManager.enableNetwork(targetNetwork.networkId, true)
        wifiManager.reconnect()
        Log.d("qqq", "✅ Đã chuyển sang Wi-Fi: $ssid")
        true
    } else {
        Log.e("qqq", "❌ Không tìm thấy Wi-Fi đã lưu: $ssid")
        false
    }
}
