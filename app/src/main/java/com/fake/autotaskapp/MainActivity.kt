package com.fake.autotaskapp

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fake.autotaskapp.MainActivity.Companion.wifiList
import com.fake.autotaskapp.floatingbarservice.FloatingBarService
import com.fake.autotaskapp.ui.theme.AutoTaskAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    companion object {
        val wifiList = listOf(
            "Pham Ki" to "mottoitam",
            "Kho" to "mottoitam"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Yêu cầu quyền nếu chưa có
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_WIFI_STATE
            ),
            100
        )

//        if (!isAccessibilityServiceEnabled(this, AutoAccessibilityService::class.java)) {
//            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            startActivity(intent)
//            Toast.makeText(this, "Vui lòng bật Trợ năng cho ứng dụng", Toast.LENGTH_LONG).show()
//        }

        setContent {
            AutoTaskAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        modifier = Modifier
                            .padding(innerPadding),
                        onItemCLicked = {
                            AutoAccessibilityService.currentSsid = it.first
                            startAutoTask(this, it)
                        }
                    )
                }
            }
        }
    }
}

fun startAutoTask(context: Context, pair: Pair<String, String>) {
    // Bắt đầu quy trình
    CoroutineScope(Dispatchers.Main).launch {
        Log.d("AutoService", "connect wifi: ${Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            connectWifiApi29(context, pair)
        } else {
            connectWifi(context, pair)
        }
        delay(5000)
        Log.d("AutoService", "startAutoTask: mo app")
        WifiHelper.openApp(context, "com.dapp.metablox")
    }
}

fun isAccessibilityServiceEnabled(
    context: Context,
    serviceClass: Class<out AccessibilityService>
): Boolean {
    val expectedComponentName = ComponentName(context, serviceClass)
    val enabledServicesSetting = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(enabledServicesSetting ?: return false)
    for (component in colonSplitter) {
        if (ComponentName.unflattenFromString(component) == expectedComponentName) {
            return true
        }
    }
    return false
}

@RequiresApi(Build.VERSION_CODES.Q)
fun connectWifiApi29(context: Context, wifi: Pair<String, String>) {
    val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
        .setSsid(wifi.first) // hoặc .setSsidPattern(...)
        .setWpa2Passphrase(wifi.second)
        .build()

    val networkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .setNetworkSpecifier(wifiNetworkSpecifier)
        .build()

    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            connectivityManager.bindProcessToNetwork(network) // Tùy chọn: gắn kết quá trình với mạng đó
        }

        override fun onUnavailable() {
            super.onUnavailable()
            context.handlerToast("Không thể kết nối wifi")
        }
    }
    connectivityManager.requestNetwork(networkRequest, networkCallback)
}

fun connectWifi(context: Context, pair: Pair<String, String>) {
    val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    val wifiConfig = WifiConfiguration().apply {
        SSID = pair.first
        preSharedKey = pair.second
    }

    val networkId = wifiManager.addNetwork(wifiConfig)
    wifiManager.disconnect()
    wifiManager.enableNetwork(networkId, true)
    wifiManager.reconnect()
}

@Composable
fun Greeting(
    modifier: Modifier = Modifier,
    onItemCLicked: (Pair<String, String>) -> Unit = {}
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Button({
//            onRunning()
//            WifiHelper.openApp(context, "com.dapp.metablox")
            context.startService(Intent(context, FloatingBarService::class.java))

        }) {
            Text("Chay toan bo")
        }

        wifiList.forEachIndexed { index, pair ->
            Text(
                modifier = Modifier
                    .clickable { onItemCLicked(pair) }
                    .padding(20.dp),
                text = "$index: SSID:${pair.first} - Password:****"//${pair.second}
            )
        }
    }
}

fun onRunning() {
//    CoroutineScope(Dispatchers.IO).launch {
//        var isRun = true
//        while (isRun) {
//            access?.rootInActiveWindow?.let { accessiblityRoot ->
////                val btnCheckIn = accessiblityRoot.findAccessibilityNodeInfosByText("Check-In")
//                val btnCheckIn =
//                    accessiblityRoot.findAccessibilityNodeInfosByViewId("com.dapp.metablox:id/rtv_event")
//                if (btnCheckIn.isNotEmpty()) {
//                    btnCheckIn.firstOrNull()?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
//                    isRun = false
//                    Log.d("qqq", "click ok")
//                    val rect = Rect()
//                    btnCheckIn.firstOrNull()?.getBoundsInScreen(rect)
//                    val x = rect.centerX()
//                    val y = rect.centerY()
//                } else {
//                    Log.d("qqq", "dang tim nut check in")
//                    delay(2000)
//                }
//            }
//        }
//    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AutoTaskAppTheme {
        Greeting()
    }
}