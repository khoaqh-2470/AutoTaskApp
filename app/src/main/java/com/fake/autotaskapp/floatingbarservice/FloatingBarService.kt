package com.fake.autotaskapp.floatingbarservice

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.fake.autotaskapp.AppEvent
import com.fake.autotaskapp.AutoAccessibilityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FloatingBarService : Service() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private val windowManager get() = getSystemService(WINDOW_SERVICE) as WindowManager
    private lateinit var floatingView: ComposeView
    private lateinit var lifecycleOwner: MyLifecycleOwner

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        val autoService = AutoAccessibilityService()
        showOverlay(
            onActionClick = {
                scope.launch {
                    autoService.autoRunCheckinWithWifiList()
                }
            },
            onCloseClick = { stopSelf() }
        )
    }

    private fun showOverlay(
        onActionClick: () -> Unit = {},
        onCloseClick: () -> Unit = {}
    ) {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.START
            x = 0
            y = 0
        }
        floatingView = ComposeView(this)
        floatingView.setContent {
            FloatingBarContent(onActionClick, onCloseClick)
        }

        lifecycleOwner = MyLifecycleOwner()
        lifecycleOwner.performRestore(null)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        floatingView.setViewTreeLifecycleOwner(lifecycleOwner)
        floatingView.setViewTreeSavedStateRegistryOwner(lifecycleOwner)

        windowManager.addView(floatingView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        if (floatingView.parent != null) {
            floatingView.clearAnimation()
            floatingView.setViewTreeLifecycleOwner(null)
            floatingView.setViewTreeSavedStateRegistryOwner(null)
            windowManager.removeView(floatingView)
        }
    }
}

@Composable
fun FloatingBarContent(onActionClick: () -> Unit = {}, onCloseClick: () -> Unit = {}) {
    val textState by AppEvent.textState.collectAsStateWithLifecycle()
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF333333),
        modifier = Modifier.padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(text = textState)
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(thickness = 1.dp, modifier = Modifier.width(24.dp))
            Spacer(Modifier.height(12.dp))
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        onActionClick()
                    })
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(thickness = 1.dp, modifier = Modifier.width(24.dp))
            Spacer(Modifier.height(12.dp))
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        onCloseClick()
                    })
        }
    }
}

@Composable
@Preview
fun FloatingBarContentPreview() {
    FloatingBarContent()
}