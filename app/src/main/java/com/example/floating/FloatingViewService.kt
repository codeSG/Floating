package com.example.floating

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.*
import android.view.*
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.app.NotificationCompat

class FloatingViewService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var params: WindowManager.LayoutParams? = null

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()

        startForegroundServiceProperly()

        floatingView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params?.gravity = Gravity.TOP or Gravity.START
        params?.x = 0
        params?.y = 100

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager?.addView(floatingView, params)

        val closeBtn = floatingView?.findViewById<ImageView>(R.id.close_btn)
        val textView = floatingView?.findViewById<TextView>(R.id.overlay_text)
        val scrollView = floatingView?.findViewById<ScrollView>(R.id.scroll_view)
        closeBtn?.setOnClickListener {
            stopSelf()
        }

        // Drag logic
        floatingView?.findViewById<View>(R.id.root_container)?.setOnTouchListener(object :
            View.OnTouchListener {

            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {

                    MotionEvent.ACTION_DOWN -> {
                        initialX = params?.x ?: 0
                        initialY = params?.y ?: 0
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        val xDiff = (event.rawX - initialTouchX).toInt()
                        val yDiff = (event.rawY - initialTouchY).toInt()

                        if (xDiff < 10 && yDiff < 10) {
                            val intent = Intent(this@FloatingViewService, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        params?.x = initialX + (event.rawX - initialTouchX).toInt()
                        params?.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager?.updateViewLayout(floatingView, params)
                        return true
                    }
                }
                return false
            }
        })

        // 🔥 Update overlay text continuously
        Handler(Looper.getMainLooper()).post(object : Runnable {
            override fun run() {

                val formattedText = StringBuilder()

                FloatingDataHolder.messages.forEach { msg ->
                    formattedText.append("━━━━━━━━━━━━━━\n")
                    formattedText.append(msg)
                    formattedText.append("\n\n")
                }

                textView?.text = formattedText.toString()

                scrollView?.post {
                    scrollView.fullScroll(View.FOCUS_DOWN)
                }

                Handler(Looper.getMainLooper()).postDelayed(this, 500)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (floatingView != null) windowManager?.removeView(floatingView)
    }

    // ✅ FIXED foreground service (NO CRASH)
    private fun startForegroundServiceProperly() {

        val channelId = "floating_service_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Floating Service",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Floating running")
            .setContentText("Overlay active")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .build()

        // 🚨 IMPORTANT: No service type passed → avoids Android 14+ crash
        startForeground(1, notification)
    }
}