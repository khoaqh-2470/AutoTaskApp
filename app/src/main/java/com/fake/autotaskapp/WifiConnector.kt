package com.fake.autotaskapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

object WifiConnector {

    private const val TAG = "WifiConnector"

    /**
     * Hàm chính để kết nối đến một mạng Wi-Fi đã lưu bằng SSID.
     * Hàm này sẽ tự động chọn phương thức phù hợp với phiên bản Android.
     *
     * @param context Context của ứng dụng.
     * @param ssid SSID của mạng Wi-Fi muốn kết nối.
     */
    fun connectToSavedWifi(context: Context, ssid: String) {
        // Kiểm tra xem Wi-Fi có đang bật không
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            Log.e(TAG, "Wi-Fi is disabled. Please enable it first.")
            // Tùy chọn: bạn có thể hiển thị Toast hoặc dialog thông báo cho người dùng
            // Toast.makeText(context, "Vui lòng bật Wi-Fi", Toast.LENGTH_SHORT).show()
            return
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            connectLegacy(context, ssid)
        } else {
            connectQAndAbove(context, ssid)
        }
    }

    /**
     * Phương thức kết nối cho Android 9 (Pie) và cũ hơn.
     * Phương thức này có thể kết nối trực tiếp trong nền.
     */
    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    private fun connectLegacy(context: Context, ssid: String) {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val configuredNetworks = wifiManager.configuredNetworks

        if (configuredNetworks.isNullOrEmpty()) {
            Log.e(TAG, "No configured Wi-Fi networks found.")
            return
        }

        // SSID trong WifiConfiguration thường được bao quanh bởi dấu ngoặc kép ""
        val targetSsid = "\"$ssid\""

        val targetNetwork = configuredNetworks.find { it.SSID == targetSsid }

        if (targetNetwork != null) {
            Log.d(TAG, "Found network: ${targetNetwork.SSID}, trying to connect...")
            // Ngắt kết nối mạng hiện tại và kết nối đến mạng mới
            wifiManager.disconnect()
            val success = wifiManager.enableNetwork(targetNetwork.networkId, true)
            wifiManager.reconnect()

            if (success) {
                Log.d(TAG, "Successfully requested connection to $ssid")
            } else {
                Log.e(TAG, "Failed to request connection to $ssid")
            }
        } else {
            Log.e(TAG, "Wi-Fi network '$ssid' is not saved on this device.")
        }
    }

    /**
     * Phương thức kết nối cho Android 10 (Q) và mới hơn.
     * Phương thức này sẽ hiển thị một panel của hệ thống để người dùng xác nhận.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectQAndAbove(context: Context, ssid: String) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val specifier = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            // Không cần cung cấp mật khẩu nếu mạng đã được lưu
            // .setWpa2Passphrase("your_password_if_needed")
            .build()

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(specifier)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                super.onAvailable(network)
                Log.d(TAG, "Network available: $network")
                // Kết nối thành công, ràng buộc app với mạng này
                connectivityManager.bindProcessToNetwork(network)
                Log.d(TAG, "Successfully connected to $ssid")

                // Quan trọng: Hủy đăng ký callback để tránh leak
                // và để ứng dụng có thể sử dụng các mạng khác (như 4G) khi Wi-Fi mất kết nối
                connectivityManager.unregisterNetworkCallback(this)
            }

            override fun onUnavailable() {
                super.onUnavailable()
                Log.e(TAG, "Could not connect to $ssid. The network may be out of range or the user cancelled.")
                // Bạn cũng nên hủy đăng ký callback ở đây
                try {
                    connectivityManager.unregisterNetworkCallback(this)
                } catch (e: IllegalArgumentException) {
                    // Bỏ qua lỗi nếu callback đã được hủy
                }
            }
        }

        Log.d(TAG, "Requesting connection to $ssid on Android 10+...")
        connectivityManager.requestNetwork(request, networkCallback)
        // Một panel hệ thống sẽ hiện lên yêu cầu người dùng chọn và kết nối
    }
}