package com.fake.autotaskapp.splash

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.net.toUri
import com.fake.autotaskapp.AutoAccessibilityService
import com.fake.autotaskapp.MainActivity

class SplashActivity : ComponentActivity() {

    companion object {
        const val REQUEST_CODE_DRAW_OVER_OTHER_APPS = 1111
        const val REQUEST_CODE_ACCESSIBILITY = 2222
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkDrawOverOtherApps()
    }

    private fun checkDrawOverOtherApps() {
        if (Settings.canDrawOverlays(this)) {
            checkAccessibilitySettings()
            return
        }
        val myIntent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            "package:$packageName".toUri()
        )
        startActivityForResult(myIntent, REQUEST_CODE_DRAW_OVER_OTHER_APPS)
    }

    private fun checkAccessibilitySettings() {
        if (isAccessibilityEnable()) {
            goToMain()
        } else {
            openAccessibilitySettings()
        }
    }

    private fun openAccessibilitySettings() {
        startActivityForResult(
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),
            REQUEST_CODE_ACCESSIBILITY
        )
    }

    private fun isAccessibilityEnable(): Boolean {
        return isAccessibilityServiceEnabled(this, AutoAccessibilityService::class.java)
//        val setting = Settings.Secure.getString(
//            applicationContext.contentResolver,
//            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
//        ) ?: return false
//        return setting.contains(getString(R.string.app_name))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_DRAW_OVER_OTHER_APPS -> {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "can cap quyen canDrawOverlays", Toast.LENGTH_SHORT).show()
                    return
                }
                checkAccessibilitySettings()
            }

            REQUEST_CODE_ACCESSIBILITY -> {
                if (!isAccessibilityEnable()) {
                    Toast.makeText(this, "can cap quyen isAccessibilityEnable", Toast.LENGTH_SHORT)
                        .show()
                    return
                }
                goToMain()
            }
        }
    }

    private fun goToMain() {
        finish()
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun isAccessibilityServiceEnabled(
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
}