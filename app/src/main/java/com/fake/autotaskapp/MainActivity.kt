package com.fake.autotaskapp

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fake.autotaskapp.MainActivity.Companion.wifiList
import com.fake.autotaskapp.data.WifiItem
import com.fake.autotaskapp.data.WifiStorage
import com.fake.autotaskapp.ui.theme.AutoTaskAppTheme

class MainActivity : ComponentActivity() {
    companion object {
        var wifiList = listOf(
            WifiItem("Pham Ki", "mottoitam"),
            WifiItem("Kho", "mottoitam"),
            WifiItem("Nha Tuan", "mottoitam"),
            WifiItem("Nha Tuan", "mottoitam"),
            WifiItem("Nha Tuan", "mottoitam"),
            WifiItem("Nha Tuan", "mottoitam"),
            WifiItem("Nha Tuan", "mottoitam"),
            WifiItem("Nha Tuan", "mottoitam"),
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
            ), 100
        )
        val autoAccessibilityService = AutoAccessibilityService()
        setContent {
            AutoTaskAppTheme {
                val context = LocalContext.current
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        var wifiList by remember { mutableStateOf(WifiStorage.getWifiList(context)) }
                        var packageList by remember {
                            mutableStateOf(
                                WifiStorage.getPackageList(
                                    context
                                )
                            )
                        }
                        MainContent(onItemCLicked = {
//                            autoAccessibilityService.testConnectWifi(it)
//                            autoAccessibilityService.autoSSID(it)
                            Toast.makeText(this@MainActivity, "no-ops", Toast.LENGTH_SHORT).show()
                        }, onAllCLicked = {
                            autoAccessibilityService.runAllPackage(wifiList, packageList)
                        }, onItemPackage = {
                            autoAccessibilityService.autoRunCheckinWithWifiList(wifiList, it)
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(
    modifier: Modifier = Modifier,
    onItemCLicked: (WifiItem) -> Unit = {},
    onAllCLicked: () -> Unit = {}
) {
    val context = LocalContext.current
    var ssid by remember { mutableStateOf("") }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = ssid,
                onValueChange = { ssid = it },
                label = { Text("Wi-Fi SSID") },
                modifier = Modifier.wrapContentSize()
            )
            Spacer(Modifier.width(8.dp))
            Button({
//            context.startService(Intent(context, FloatingBarService::class.java))
//                context.handlerToast("Chay toan bo")
            }) {
                Text("Lưu")
            }
        }
        Button({
//            context.startService(Intent(context, FloatingBarService::class.java))
            context.handlerToast("Chay toan bo")
        }) {
            Text("Chay toan bo")
        }
        wifiList.forEachIndexed { index, item ->
            Text(
                modifier = Modifier
                    .clickable { onItemCLicked(item) }
                    .padding(20.dp),
                text = "$index: SSID:${item.ssid} - Password:****"//${pair.second}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AutoTaskAppTheme {
//        Greeting()
        MainContent()
    }
}

@Composable
fun MainContent(
    onItemCLicked: (WifiItem) -> Unit = {},
    onItemPackage: (String) -> Unit = {},
    onAllCLicked: (List<WifiItem>) -> Unit = {}
) {
    val context = LocalContext.current
    var ssid by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
//    var wifiList by remember { mutableStateOf(wifiList) }
//    var packageList by remember {
//        mutableStateOf(
//            listOf(
//                "123123", "123123", "123123", "123123", "12312312", "123123"
//            )
//        )
//    }
    var wifiList by remember { mutableStateOf(WifiStorage.getWifiList(context)) }
    var packageList by remember { mutableStateOf(WifiStorage.getPackageList(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = ssid,
                onValueChange = { ssid = it },
                label = { Text("Wi-Fi SSID") },
                modifier = Modifier.fillMaxWidth(0.5f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (ssid.isNotBlank()) {
                        val newItem = WifiItem(ssid)
                        val newList = wifiList.toMutableList().apply { add(newItem) }
                        WifiStorage.saveWifiList(context, newList)
                        wifiList = newList
                        ssid = ""
                        Toast.makeText(context, "Đã lưu Wi-Fi", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show()
                    }
                }, modifier = Modifier
            ) {
                Text("Lưu")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = packageName,
                onValueChange = { packageName = it },
                label = { Text("dkapp.xxx.xxx") },
                modifier = Modifier.fillMaxWidth(0.5f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (packageName.isNotBlank()) {
                        val newList = packageList.toMutableList().apply { add(packageName) }
                        WifiStorage.savPackageList(context, newList)
                        packageList = newList
                        packageName = ""
                        Toast.makeText(context, "Đã lưu package", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show()
                    }
                }, modifier = Modifier
            ) {
                Text("Lưu package")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                onAllCLicked(wifiList)
            }, modifier = Modifier
        ) {
            Text("Run All")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("📡 Danh sách Wi-Fi đã lưu:", style = MaterialTheme.typography.titleMedium)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            wifiList.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .clickable {
                            onItemCLicked(item)
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("SSID: ${item.ssid}")
                    Button(onClick = {
                        val newList = wifiList.toMutableList().apply { removeAt(index) }
                        WifiStorage.saveWifiList(context, newList)
                        wifiList = newList
                        Toast.makeText(context, "Đã xoá Wi-Fi", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Xoá")
                    }
                }
            }
        }
//        LazyColumn(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
//            itemsIndexed(wifiList) { index, item ->
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(4.dp)
//                        .clickable {
//                            onItemCLicked(item)
//                        },
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text("SSID: ${item.ssid}")
//                    Button(onClick = {
//                        val newList = wifiList.toMutableList().apply { removeAt(index) }
//                        WifiStorage.saveWifiList(context, newList)
//                        wifiList = newList
//                        Toast.makeText(context, "Đã xoá Wi-Fi", Toast.LENGTH_SHORT).show()
//                    }) {
//                        Text("Xoá")
//                    }
//                }
//            }
//        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("📡 Danh sách Package đã lưu:", style = MaterialTheme.typography.titleMedium)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            packageList.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .clickable {
                            onItemPackage(item)
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Package: ${item}")
                    Button(onClick = {
                        val newList = packageList.toMutableList().apply { removeAt(index) }
                        WifiStorage.savPackageList(context, newList)
                        packageList = newList
                        Toast.makeText(context, "Đã xoá Package", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Xoá")
                    }
                }
            }
//            itemsIndexed(packageList) { index, item ->
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(4.dp)
//                        .clickable {
//                            onItemPackage(item)
//                        },
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text("Package: ${item}")
//                    Button(onClick = {
//                        val newList = packageList.toMutableList().apply { removeAt(index) }
//                        WifiStorage.savPackageList(context, newList)
//                        packageList = newList
//                        Toast.makeText(context, "Đã xoá Package", Toast.LENGTH_SHORT).show()
//                    }) {
//                        Text("Xoá")
//                    }
//                }
        }
    }
}